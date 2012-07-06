package com.stackmob.oauthhttpservice;

import org.scribe.services.TimestampService;
import org.scribe.services.TimestampServiceImpl;
import org.scribe.model.Token;
import org.scribe.builder.api.DefaultApi10a;

public class HttpRequestSignerAPI extends DefaultApi10a {
	public static class TimeService extends TimestampServiceImpl {
        @Override
        public String getTimestampInSeconds() {
			return String.valueOf(System.currentTimeMillis());
        }
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(Token token) {
        return null;
    }

    @Override
    public TimestampService getTimestampService()
    {
        return new TimeService();
    }
}