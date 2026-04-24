package com.faible.coplate; // Ваш пакет

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Family extends Fragment {

    public Family() {
        super(R.layout.fragment_family);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация аккордеона (ваш существующий код)
        Button createBtn = view.findViewById(R.id.createFamilyButton);
        Button joinBtn = view.findViewById(R.id.joinFamilyButton);
        LinearLayout createContent = view.findViewById(R.id.createFamilyContent);
        LinearLayout joinContent = view.findViewById(R.id.joinFamilyContent);

        if (createBtn != null && joinBtn != null) {
            createBtn.setOnClickListener(v -> toggleFamilyContent(createContent, joinContent));
            joinBtn.setOnClickListener(v -> toggleFamilyContent(joinContent, createContent));
        }

        // 2. ✅ ВАЖНО: Вызов метода инициализации кнопки настроек
        initSettingsButton(view);
    }

    private void initSettingsButton(View view) {
        View settingsBtn = view.findViewById(R.id.settingsButton);

        if (settingsBtn == null) {
            Toast.makeText(requireContext(), "ОШИБКА: settingsButton равен NULL", Toast.LENGTH_LONG).show();
            return;
        }

        settingsBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "КЛИК ПРОЙДЕН! Открываю диалог...", Toast.LENGTH_SHORT).show();

            try {
                SettingsDialogFragment dialog = new SettingsDialogFragment();
                dialog.show(getChildFragmentManager(), SettingsDialogFragment.TAG);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Ошибка открытия: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void toggleFamilyContent(LinearLayout show, LinearLayout hide) {
        boolean isVisible = show.getVisibility() == View.VISIBLE;
        hide.setVisibility(View.GONE);
        show.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }
}