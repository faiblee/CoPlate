package com.faible.coplate.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.MainActivity;
import com.faible.coplate.R;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Кнопка "Войти"
        View btnLogin = view.findViewById(R.id.btnLogin);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Вход...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            });
        } else {
            Toast.makeText(requireContext(), "Кнопка не найдена!", Toast.LENGTH_LONG).show();
        }
    }
}