package com.bpow.malupdater.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.bpow.malupdater.R;
import com.bpow.malupdater.request.AuthenticationRequest;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    private boolean mCustomTabsOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Not triggered if Accept or Cancel is selected, only if back button or
        // Exit is selected from Custom Tabs instance. This is because
        // handleIntent is called before onResume, and it sets this to false.
        if (mCustomTabsOpened){
            mCustomTabsOpened = false;

            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }else{
            goToLoginPage();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        mCustomTabsOpened = false;

        String code = null;
        if (intent.getData() != null){
            code = intent.getData().getQueryParameter("code");
        }

        if (code == null){return;}

        AuthenticationRequest.getAccessToken(this, code, response -> {
            AuthenticationRequest.storeAccessToken(LoginActivity.this, response);
            MainActivity.loadUserList(LoginActivity.this, UserListActivity.ListType.ANIME);
        });
    }

    private void goToLoginPage(){
        String codeChallenge;
        String codeVerifier = codeChallenge = AuthenticationRequest.getNewCodeVerifier();

        sharedPref = getSharedPreferences("malupdaterprefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("codeVerifier", codeVerifier);
        editor.apply();

        String authUrl = AuthenticationRequest.getLoginURL(this, codeChallenge);

        mCustomTabsOpened = true;
        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = customTabBuilder.build();
        customTabsIntent.launchUrl(this, Uri.parse(authUrl));
    }

}
