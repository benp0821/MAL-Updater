package com.bpow.malupdater.request;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Base64;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AuthenticationRequest {

    private static String CLIENT_ID = null;

    private static String getClientId(Context context){

        if (CLIENT_ID == null){
            Properties prop = new Properties();
            AssetManager assetManager = context.getAssets();
            String fileName = "client.config";

            try (InputStream is = assetManager.open(fileName)) {
                prop.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            CLIENT_ID = prop.getProperty("CLIENT_ID");
            System.out.println(CLIENT_ID);
        }

        return CLIENT_ID;
    }

    private static Pair<String, HashMap<String, String>> constructAuthRequest(String endOfUrl, HashMap<String, String> queryParameters){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("myanimelist.net")
                .appendPath("v1")
                .appendPath("oauth2");

        if (endOfUrl != null) {
            builder.appendPath(endOfUrl);
        }

        if (queryParameters != null) {
            for (Map.Entry<String, String> queryParameter : queryParameters.entrySet()) {
                builder.appendQueryParameter(queryParameter.getKey(), queryParameter.getValue());
            }
        }

        String url = builder.build().toString();

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        return new Pair<>(url, headers);
    }

    public static String getLoginURL(Context context, String codeChallenge){
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("response_type", "code");
        queryParams.put("client_id", getClientId(context));
        queryParams.put("code_challenge", codeChallenge);

        return constructAuthRequest("authorize", queryParams).first;
    }

    public static void getAccessToken(Context context, String code, Response.Listener<String> listener){
        SharedPreferences sharedPref = context.getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);

        HashMap<String, String> params = new HashMap<>();
        params.put("client_id", getClientId(context));
        params.put("code", code);
        params.put("code_verifier", sharedPref.getString("codeVerifier", ""));
        params.put("grant_type", "authorization_code");

        Pair<String, HashMap<String, String>> urlWithHeaders = constructAuthRequest("token", null);

        HttpRequest.sendRequest(context, Request.Method.POST, urlWithHeaders.first, params, urlWithHeaders.second, listener);
    }

    public static void refreshAccessToken(Context context, Response.Listener<String> listener){
        SharedPreferences sharedPref = context.getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);

        HashMap<String, String> params = new HashMap<>();
        params.put("client_id", getClientId(context));
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", sharedPref.getString("refresh_token", ""));

        Pair<String, HashMap<String, String>> urlWithHeaders = constructAuthRequest("token", null);

        HttpRequest.sendRequest(context, Request.Method.POST, urlWithHeaders.first, params, urlWithHeaders.second, listener);
    }

    public static void storeAccessToken(Context context, String response){
        try {
            SharedPreferences sharedPref = context.getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            JSONObject responseObject = new JSONObject(response);

            editor.putString("token_type", responseObject.getString("token_type"));
            editor.putString("expires_in", responseObject.getString("expires_in"));
            editor.putString("access_token", responseObject.getString("access_token"));
            editor.putString("refresh_token", responseObject.getString("refresh_token"));
            editor.putLong("renewal_time", Calendar.getInstance().getTimeInMillis());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getNewCodeVerifier(){
        SecureRandom secureRandomGenerator = null;
        try {
            Provider[] secureRandomProviders = Security.getProviders("SecureRandom.SHA1PRNG");
            secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", secureRandomProviders[0].getName());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        // Get 128 random bytes
        byte[] randomBytes = new byte[100];
        if (secureRandomGenerator != null) {
            secureRandomGenerator.nextBytes(randomBytes);
        }

        return Base64.encodeToString(randomBytes, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE).substring(0, 128);
    }

}
