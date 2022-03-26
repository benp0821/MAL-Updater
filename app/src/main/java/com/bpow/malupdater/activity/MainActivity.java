package com.bpow.malupdater.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bpow.malupdater.request.AuthenticationRequest;

import java.util.Calendar;


public class MainActivity extends Activity {

    public static final long MAX_RENEWAL_TIME = 2678400000L - 259200000L; //31 - 3 days in milliseconds. API refresh token expires after 31 days

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);

        if (canSkipLogin(sharedPref)) {
            AuthenticationRequest.refreshAccessToken(this, response -> {
                AuthenticationRequest.storeAccessToken(MainActivity.this, response);
                loadUserList(this, UserListActivity.ListType.ANIME);
            });
        }else{
            loadLoginPage(this);
        }

        finish();
    }

    public static boolean canSkipLogin(SharedPreferences sharedPref){

        String access_token = sharedPref.getString("access_token", "");

        long currentTime = Calendar.getInstance().getTimeInMillis();
        long timeOfLastRenewal = sharedPref.getLong("renewal_time", -1);

        return !access_token.equals("") && currentTime - MAX_RENEWAL_TIME <= timeOfLastRenewal;
    }

    public static void loadUserList(Context context, UserListActivity.ListType listType){
        Intent intent = new Intent(context, UserListActivity.class);
        intent.putExtra("type", listType);
        context.startActivity(intent);
    }

    public static void loadLoginPage(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}
