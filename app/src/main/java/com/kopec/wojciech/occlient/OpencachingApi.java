package com.kopec.wojciech.occlient;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * Created by Wojtek on 2016-11-30.
 */

public class OpencachingApi extends DefaultApi10a {
    private static final String AUTHORIZE_URL = "http://opencaching.pl/okapi/services/oauth/authorize?oauth_token=%s";

    public OpencachingApi(){
    }

    private static class InstanceHolder {
        private static final OpencachingApi INSTANCE = new OpencachingApi();
    }

    public static OpencachingApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "http://opencaching.pl/okapi/services/oauth/request_token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "http://opencaching.pl/okapi/services/oauth/access_token";
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return String.format(AUTHORIZE_URL, requestToken.getToken());
    }
}
