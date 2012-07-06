package com.stackmob.oauthhttpservice;

import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.stackmob.sdkapi.http.*;
import com.stackmob.sdkapi.http.request.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.net.MalformedURLException;

class HttpRequestSigner {
	
	private String apiKey;
	private String apiSecret;
	private String appName;
	private int apiVersionNum;
	private OAuthService oAuthService;
	private static final String userAgentName = "Custom Code HttpService";
	
	//pass the API key, secret, and version here for the stackmob app that you want to call
	public HttpRequestSigner(String apiKey, String apiSecret, String appName, int apiVersionNum) throws OAuthException {
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.appName = appName;
		this.apiVersionNum = apiVersionNum;
		this.oAuthService = new ServiceBuilder().provider(HttpRequestSignerAPI.class).apiKey(apiKey).apiSecret(apiSecret).build();
	}
	
	private String getUserAgentString() {
		return String.format("StackMob (%s; %d)%s", userAgentName, apiVersionNum, "/"+appName);
	}
	
	private String getAcceptString() {
		return "application/vnd.stackmob+json; version="+apiVersionNum;
	}
	
	private static Verb calculateVerb(HttpRequest req) {
		if(req instanceof GetRequest) return Verb.GET;
		else if(req instanceof PostRequest) return Verb.POST;
		else if(req instanceof PutRequest) return Verb.PUT;
		else return Verb.DELETE;
	}
	
	private static Set<Header> convertToHeaderSet(Map<String, String> map) {
		Set<Header> ret = new HashSet<Header>();
		for(Map.Entry<String, String> entry: map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			ret.add(new Header(key, value));
		}
		return ret;
	}
	
	private OAuthRequest createAndSignOAuthRequest(HttpRequest inputRequest) {
		String url = inputRequest.getUrl().toString();
		Verb verb = calculateVerb(inputRequest);
		OAuthRequest oReq = new OAuthRequest(verb, url);
		
		//construct complete header list
		List<Header> headerList = new ArrayList<Header>();
		if(!verb.equals(Verb.GET) && !verb.equals(Verb.DELETE)) {
			headerList.add(new Header("Content-Type", "application/json"));
        }
		headerList.add(new Header("Accept", getAcceptString()));
		headerList.add(new Header("User-Agent", getUserAgentString()));
		headerList.addAll(inputRequest.getHeaders());
		
		//add headers to request
        for(Header header: headerList) {
			oReq.addHeader(header.getName(), header.getValue());
		}
		
		//sign and return request
		oAuthService.signRequest(new Token("", ""), oReq);
		return oReq;
	}
		
	public HttpRequestWithoutBody sign(HttpRequestWithoutBody unsigned) throws MalformedURLException {
		OAuthRequest req = createAndSignOAuthRequest(unsigned);
		String url = unsigned.getUrl().toString();
		Set<Header> completeHeaders = convertToHeaderSet(req.getHeaders());
		if(unsigned instanceof GetRequest) return new GetRequest(url, completeHeaders);
		else return new DeleteRequest(url, completeHeaders);
	}
	
	public HttpRequestWithBody sign(HttpRequestWithBody unsigned) throws MalformedURLException {
		OAuthRequest req = createAndSignOAuthRequest(unsigned);
		String url = unsigned.getUrl().toString();
		Set<Header> completeHeaders = convertToHeaderSet(req.getHeaders());
		String body = unsigned.getBody();
		if(unsigned instanceof PostRequest) return new PostRequest(url, completeHeaders, body);
		else return new PutRequest(url, completeHeaders, body);
	}
}