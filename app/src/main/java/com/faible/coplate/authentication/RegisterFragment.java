package com.faible.coplate.authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText usernameInput = view.findViewById(R.id.etUsername);
        EditText passwordInput = view.findViewById(R.id.etPassword);

        // Кнопка "Подтвердить" (заглушка регистрации)
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Здесь позже будет реальная регистрация через API
            Toast.makeText(requireContext(), "Регистрация успешно завершена (заглушка)", Toast.LENGTH_SHORT).show();

            // Опционально: вернуться к экрану входа
            // ((AuthActivity) requireActivity()).openAuthScreen(new LoginFragment());
        });
    }
}