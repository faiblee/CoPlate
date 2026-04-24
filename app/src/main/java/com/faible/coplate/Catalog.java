package com.faible.coplate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Catalog extends Fragment {
    public Catalog() {
        super(R.layout.fragment_catalog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSettingsButton(view);
        // Здесь позже будет логика списка покупок
    }
    private void initSettingsButton(View view) {
        ImageButton settingsBtn = view.findViewById(R.id.settingsButton);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                // Создаем экземпляр диалога
                SettingsDialogFragment dialog = new SettingsDialogFragment();

                // Показываем его.
                // getChildFragmentManager() используется, если мы внутри фрагмента.
                // Если бы мы были в Activity, использовали бы getSupportFragmentManager().
                dialog.show(getChildFragmentManager(), SettingsDialogFragment.TAG);
            });
        }
    }
}
