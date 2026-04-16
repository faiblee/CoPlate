package com.faible.coplate.authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;
import com.faible.coplate.api.AuthApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.AuthResponse;
import com.faible.coplate.model.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar; // Опционально, если есть в layout

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация views
        etUsername = view.findViewById(R.id.etUsername); // Проверьте ID в XML
        etPassword = view.findViewById(R.id.etPassword); // Проверьте ID в XML
        btnLogin = view.findViewById(R.id.btnLogin);

        // Если есть прогресс-бар для загрузки, раскомментируйте:
        // progressBar = view.findViewById(R.id.progressBar);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Валидация
        if (username.isEmpty()) {
            etUsername.setError("Введите имя пользователя");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        // Блокировка кнопки и показ загрузки (опционально)
        btnLogin.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Подготовка запроса
        LoginRequest request = new LoginRequest(username, password);

        // Получение API сервиса
        AuthApi authApi = RetrofitClient.getClient(requireContext()).create(AuthApi.class);

        // Выполнение запроса
        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                // Сброс состояния UI
                btnLogin.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();

                    // Сохранение токена и данных через метод в AuthActivity
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).loginSuccess(
                                authData.getToken(),
                                authData.getId(),
                                authData.getUsername(),
                                authData.getName()
                        );
                    }
                } else {
                    // Обработка ошибок сервера (401, 403 и т.д.)
                    String errorMsg = "Ошибка входа";
                    if (response.errorBody() != null) {
                        try {
                            // Можно распарсить errorBody для получения точного сообщения от сервера
                            errorMsg = "Неверный логин или пароль";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                // Сброс состояния UI
                btnLogin.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}