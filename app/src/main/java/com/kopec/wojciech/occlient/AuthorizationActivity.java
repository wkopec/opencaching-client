package com.kopec.wojciech.occlient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AuthorizationActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private WebView mWebView;
    private OAuth10aService mService;
    private OAuth1RequestToken requestToken;
    public String verifier = "";
    private static String authUrl = "YOUR_AUTH_URL";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authorization);
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        mService = new ServiceBuilder()
                .apiKey(getString(R.string.OKAPIConsumerKey))
                .apiSecret(getString(R.string.OKAPIConsumerSecret))
                .build(OpencachingApi.instance());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    requestToken = mService.getRequestToken();
                    authUrl = mService.getAuthorizationUrl(requestToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                android.webkit.CookieManager cookieManager = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean aBoolean) {
                            Log.d("Cookies", "Cookie removed: " + aBoolean);
                        }
                    });
                }
                else cookieManager.removeAllCookie();

                mWebView = (WebView) findViewById(R.id.authorizationWebView);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.setWebViewClient(new WebViewClient() {
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        if(url.contains("oauth_verifier=")) {
                            mWebView.setVisibility(View.GONE);
                            url = url.substring(url.indexOf("oauth_token"));
                            String[] pairs = url.split("&");
                            String token = pairs[0].replace("oauth_token=", "");
                            verifier = pairs[1].replace("oauth_verifier=", "");
                            getOauthToken();

                        }
                    }
                });
                mWebView.loadUrl(authUrl);
            }
        }.execute();
    }

    public void getOauthToken() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final OAuth1AccessToken accessToken = mService.getAccessToken(requestToken, verifier);
                    String oauthToken = accessToken.getToken();
                    String oauthSecretToken = accessToken.getTokenSecret();

                    Log.d("Oauth Token", oauthToken);
                    Log.d("Oauth Secret Token", oauthSecretToken);

                    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://opencaching.pl/okapi/services/users/user?fields=username|uuid|profile_url", mService);
                    mService.signRequest(accessToken, request);
                    final Response response = request.send();

                    JSONObject json = new JSONObject(response.getBody());

                    SharedPreferences.Editor mEditor = sharedPreferences.edit();
                    mEditor.putString("oauth_token", oauthToken);
                    mEditor.putString("oauth_token_secret", oauthSecretToken);
                    mEditor.apply();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("username", json.getString("username"));
                    returnIntent.putExtra("user_uuid", json.getString("uuid"));
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}