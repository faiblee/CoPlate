package com.faible.coplate.authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.api.AuthApi;
import com.faible.coplate.model.AuthResponse;
import com.faible.coplate.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText etUsername;
    private EditText etName;
    private EditText etPassword;
    private ProgressBar progressBar;
    private AuthApi authApi;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация API
        authApi = RetrofitClient.getClient(requireContext()).create(AuthApi.class);

        // Инициализация Views
        etUsername = view.findViewById(R.id.etUsername);
        etName = view.findViewById(R.id.etName);
        etPassword = view.findViewById(R.id.etPassword);

        // Если у вас есть ProgressBar в XML, раскомментируйте строку ниже
        // progressBar = view.findViewById(R.id.progressBar);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) requireActivity()).openAuthScreen(new AuthStartFragment());
                }
            });
        }

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Валидация
            if (username.isEmpty()) {
                etUsername.setError("Введите логин");
                etUsername.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                etName.setError("Введите имя");
                etName.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Введите пароль");
                etPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Минимум 6 символов");
                etPassword.requestFocus();
                return;
            }

            performRegister(username, name, password);
        });
    }

    private void performRegister(String username, String name, String password) {
        // Показать индикатор загрузки (если есть)
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Формируем запрос. Роль по умолчанию "user", как в Postman
        RegisterRequest request = new RegisterRequest(username, password, name, "user");

        authApi.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Сохраняем токен и данные пользователя
                    ((AuthActivity) requireActivity()).loginSuccess(
                            authResponse.getToken(),
                            authResponse.getId(),
                            authResponse.getUsername(),
                            authResponse.getName()
                    );

                    Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                } else {
                    // Обработка ошибок сервера (400, 409 и т.д.)
                    String errorMsg = "Ошибка регистрации";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Нет соединения: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}