package com.faible.coplate;

import android.app.Dialog;
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

public class SettingsDialogFragment extends DialogFragment {

    public static final String TAG = "SettingsDialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Загружаем обновленный макет диалога
        return inflater.inflate(R.layout.fragment_settings_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация элементов управления темой
        RadioGroup themeGroup = view.findViewById(R.id.themeGroup);

        // 2. Инициализация полей ввода
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etName = view.findViewById(R.id.etName);
        EditText etOldPassword = view.findViewById(R.id.etOldPassword);
        EditText etNewPassword = view.findViewById(R.id.etPassword); // В XML это поле называется etPassword, но логически это новый пароль

        // 3. Инициализация кнопок-карандашей
        ImageButton btnEditUsername = view.findViewById(R.id.btnEditUsername);
        ImageButton btnEditName = view.findViewById(R.id.btnEditName);

        // 4. Кнопка "Применить"
        Button btnApply = view.findViewById(R.id.btnApply);

        // --- Логика переключения темы ---
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                // Логика для светлой темы (заглушка)
                Toast.makeText(requireContext(), "Выбрана светлая тема", Toast.LENGTH_SHORT).show();
            } else {
                // Логика для темной темы (заглушка)
                Toast.makeText(requireContext(), "Выбрана тёмная тема", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Логика кнопок-карандашей ---

        // Карандаш у "Имя пользователя"
        btnEditUsername.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Редактирование имени пользователя", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus(); // Переводим фокус на поле
        });

        // Карандаш у "Имя"
        btnEditName.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Редактирование имени", Toast.LENGTH_SHORT).show();
            etName.requestFocus(); // Переводим фокус на поле
        });

        // --- Логика кнопки "Применить" ---
        btnApply.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String oldPass = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
            String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";

            // Простая валидация
            if (username.isEmpty() || name.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните обязательные поля (Имя пользователя и Имя)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Если введен старый или новый пароль, проверяем их наличие вместе
            if ((!oldPass.isEmpty() && newPass.isEmpty()) || (oldPass.isEmpty() && !newPass.isEmpty())) {
                Toast.makeText(requireContext(), "Для смены пароля заполните оба поля: старый и новый пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Здесь вызов метода сохранения данных (например, в SharedPreferences или отправка на сервер)
            saveSettings(username, name, oldPass, newPass);

            Toast.makeText(requireContext(), "Настройки успешно сохранены!", Toast.LENGTH_SHORT).show();

            // Закрываем диалог
            dismiss();
        });
    }

    /**
     * Метод-заглушка для сохранения настроек.
     * В реальном приложении здесь будет работа с базой данных или API.
     */
    private void saveSettings(String username, String name, String oldPassword, String newPassword) {
        // Пример логирования
        android.util.Log.d("SettingsDialog", "Сохранение настроек:");
        android.util.Log.d("SettingsDialog", "Username: " + username);
        android.util.Log.d("SettingsDialog", "Name: " + name);
        if (!oldPassword.isEmpty()) {
            android.util.Log.d("SettingsDialog", "Password change requested.");
        }

        // Пример сохранения в SharedPreferences (раскомментируйте и адаптируйте под нужды):
        /*
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit()
             .putString("username", username)
             .putString("display_name", name)
             // Пароли обычно не хранят в SharedPreferences в открытом виде!
             .apply();
        */
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();

            // Убираем стандартный фон диалога, чтобы работал наш rounded_rectangle
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Настраиваем ширину диалога (90% от ширины экрана)
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}