package com.faible.coplate.authentication;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;

public class AuthStartFragment extends Fragment {

    public AuthStartFragment() {
        super(R.layout.fragment_auth_start);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Кнопка "Войти"
        view.findViewById(R.id.btnLogin).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).openAuthScreen(new LoginFragment()));

        // Кнопка "Зарегистрироваться"
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).openAuthScreen(new RegisterFragment()));
    }
}