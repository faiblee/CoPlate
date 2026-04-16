package com.faible.coplate.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.faible.coplate.MainActivity;
import com.faible.coplate.R;

public class AuthActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "app_prefs";
    public static final String KEY_TOKEN = "auth_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authContainer, new AuthStartFragment())
                    .commit();
        }
    }

    public void openAuthScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.authContainer, fragment)
                .commit();
    }

    public void loginSuccess(String token, String userId, String username, String name) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .remove("family_id")
                .remove("family_name")
                .remove("family_invite_code")
                .apply();
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_NAME, name)
                .apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void loginSuccess(String token, String userId) {
        loginSuccess(token, userId, "", "");
    }
}