package com.faible.coplate;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.api.UserApi;
import com.faible.coplate.authentication.AuthActivity;
import com.faible.coplate.model.UpdateUserRequest;
import com.faible.coplate.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsDialogFragment extends DialogFragment {

    public static final String TAG = "SettingsDialog";

    // UI Elements
    private EditText etUsername;
    private EditText etName;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private ImageButton btnEditUsername;
    private ImageButton btnEditName;
    private Button btnApply;
    private Button btnLogout;
    private RadioGroup themeGroup;

    // API
    private UserApi userApi;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация UI
        initViews(view);

        // 2. Инициализация API
        userApi = RetrofitClient.getClient(requireContext()).create(UserApi.class);

        // 3. Получаем ID текущего пользователя из SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // 4. Загружаем текущие данные пользователя
        loadUserData(currentUserId);

        // 5. Настраиваем обработчики событий
        setupListeners();
    }

    private void initViews(View view) {
        etUsername = view.findViewById(R.id.etUsername);
        etName = view.findViewById(R.id.etName);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etPassword); // В XML это поле называется etPassword

        btnEditUsername = view.findViewById(R.id.btnEditUsername);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnApply = view.findViewById(R.id.btnApply);
        btnLogout = view.findViewById(R.id.btnLogout);
        themeGroup = view.findViewById(R.id.themeGroup);
    }

    private void loadUserData(String userId) {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        String localUsername = prefs.getString("username", "");
        String localName = prefs.getString("name", "");
        if (localUsername != null) etUsername.setText(localUsername);
        if (localName != null) etName.setText(localName);

        // Показываем индикатор загрузки (опционально, можно заблокировать кнопку Apply)
        btnApply.setEnabled(false);
        btnApply.setText("Загрузка...");

        userApi.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                btnApply.setEnabled(true);
                btnApply.setText("Применить");

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Заполняем поля полученными данными
                    if (user.getUsername() != null) etUsername.setText(user.getUsername());
                    if (user.getName() != null) etName.setText(user.getName());

                    SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
                    prefs.edit()
                            .putString("username", user.getUsername())
                            .putString("name", user.getName())
                            .apply();
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnApply.setEnabled(true);
                btnApply.setText("Применить");
                Toast.makeText(requireContext(), "Ошибка сети при загрузке данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Карандаши просто переводят фокус на поле для редактирования
        btnEditUsername.setOnClickListener(v -> etUsername.requestFocus());
        btnEditName.setOnClickListener(v -> etName.requestFocus());

        // Кнопка "Применить" отправляет данные на сервер
        btnApply.setOnClickListener(v -> saveSettings());
        btnLogout.setOnClickListener(v -> logout());

        // Логика темы (пока заглушка, как было ранее)
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Здесь будет логика смены темы приложения
        });
    }

    private void saveSettings() {
        String newUsername = etUsername.getText().toString().trim();
        String newName = etName.getText().toString().trim();
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();

        // Валидация
        if (newUsername.isEmpty() || newName.isEmpty()) {
            Toast.makeText(requireContext(), "Имя пользователя и Имя обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка логики паролей
        if (!oldPass.isEmpty() || !newPass.isEmpty()) {
            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Для смены пароля заполните оба поля: старый и новый", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(requireContext(), "Новый пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Формируем запрос
        // Если пароли пустые, передаем null или пустую строку?
        // Лучше передать null, если бэкенд умеет игнорировать null поля.
        // Если бэкенд требует пустую строку, оставьте как есть.
        // В данном случае, если oldPass пуст, мы не должны его отправлять, если не хотим сбросить пароль.
        // Но согласно API, поля опциональны.

        String passToSendOld = oldPass.isEmpty() ? null : oldPass;
        String passToSendNew = newPass.isEmpty() ? null : newPass;

        UpdateUserRequest request = new UpdateUserRequest(newUsername, passToSendOld, passToSendNew, newName);

        // Блокируем UI на время запроса
        btnApply.setEnabled(false);
        btnApply.setText("Сохранение...");

        userApi.updateUser(currentUserId, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                btnApply.setEnabled(true);
                btnApply.setText("Применить");

                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    Toast.makeText(requireContext(), "Данные успешно обновлены!", Toast.LENGTH_SHORT).show();

                    // Обновляем локальный контекст актуальными данными от сервера.
                    SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
                    prefs.edit()
                            .putString("username", updatedUser.getUsername() != null ? updatedUser.getUsername() : newUsername)
                            .putString("name", updatedUser.getName() != null ? updatedUser.getName() : newName)
                            .apply();

                    etOldPassword.setText("");
                    etNewPassword.setText("");
                    dismiss(); // Закрываем диалог
                } else {
                    // Обработка ошибок от сервера (400, 409 и т.д.)
                    String errorMsg = "Ошибка обновления";
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
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnApply.setEnabled(true);
                btnApply.setText("Применить");
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}