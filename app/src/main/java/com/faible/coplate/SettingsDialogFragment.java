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
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsDialogFragment extends DialogFragment {

    public static final String TAG = "SettingsDialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        RadioGroup themeGroup = view.findViewById(R.id.themeGroup);
        TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        TextInputEditText etName = view.findViewById(R.id.etName);

        // Кнопка применить
        Button btnApply = view.findViewById(R.id.btnApply);

        // Логика переключения темы (заглушка)
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                Toast.makeText(requireContext(), "Выбрана светлая тема", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Выбрана тёмная тема", Toast.LENGTH_SHORT).show();
            }
        });
        // 3. Обработчик нажатия на кнопку "Применить"
        btnApply.setOnClickListener(v -> {
            // Получаем данные из полей
            String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String name = etName.getText() != null ? etName.getText().toString() : "";

            // Проверка: если поля пустые, можно предупредить (опционально)
            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Введите имя пользователя", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Здесь будет код сохранения данных (например, в SharedPreferences)
            saveSettings(username, password, name);

            // Показываем уведомление об успехе
            Toast.makeText(requireContext(), "Настройки сохранены!", Toast.LENGTH_SHORT).show();

            // Закрываем диалог
            dismiss();
        });

        // Обработка сохранения (пример)
        // В реальном приложении здесь будет сохранение в SharedPreferences или базу данных
    }
    // Метод-заглушка для сохранения (потом замените на реальный код)
    private void saveSettings(String username, String password, String name) {
        // Пример сохранения в консоль для проверки
        android.util.Log.d("Settings", "User: " + username + ", Name: " + name);

        // Если используете SharedPreferences:
        // SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        // prefs.edit().putString("username", username).apply();
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