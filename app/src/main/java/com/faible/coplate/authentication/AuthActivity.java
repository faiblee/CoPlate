package com.faible.coplate.authentication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // При первом запуске показываем стартовый экран с 2 кнопками
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authContainer, new AuthStartFragment())
                    .commit();
        }
    }

    // Переходы без backstack (как ты хочешь)
    public void openAuthScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.authContainer, fragment)
                .commit();
    }
}