package com.faible.coplate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.dishes.MyDishesFragment;

public class Day extends Fragment {

    public Day() {
        super(R.layout.fragment_day);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMealToggles(view);
        initActionButtons(view);
        initTrashButton(view);
        initSettingsButton(view);
    }

    private void initMealToggles(View view) {
        Button breakfastBtn = view.findViewById(R.id.breakfastButton);
        Button lunchBtn = view.findViewById(R.id.lunchButton);
        Button dinnerBtn = view.findViewById(R.id.dinnerButton);

        LinearLayout breakfastActions = view.findViewById(R.id.breakfastActions);
        LinearLayout lunchActions = view.findViewById(R.id.lunchActions);
        LinearLayout dinnerActions = view.findViewById(R.id.dinnerActions);

        breakfastBtn.setOnClickListener(v -> toggleMealActions(breakfastActions, lunchActions, dinnerActions));
        lunchBtn.setOnClickListener(v -> toggleMealActions(lunchActions, breakfastActions, dinnerActions));
        dinnerBtn.setOnClickListener(v -> toggleMealActions(dinnerActions, breakfastActions, lunchActions));
    }

    private void toggleMealActions(LinearLayout show, LinearLayout... hide) {
        boolean isVisible = show.getVisibility() == View.VISIBLE;

        // Скрыть все
        for (LinearLayout layout : hide) {
            layout.setVisibility(View.GONE);
        }

        // Показать нужный, если был скрыт
        show.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void initActionButtons(View view) {
        // Завтрак
        view.findViewById(R.id.addCustomDishBreakfast).setOnClickListener(v -> openMyDishesScreen());
        view.findViewById(R.id.libraryBreakfast).setOnClickListener(v ->
                showToast("Библиотека завтраков (заглушка)"));

        // Обед
        view.findViewById(R.id.addCustomDishLunch).setOnClickListener(v -> openMyDishesScreen());
        view.findViewById(R.id.libraryLunch).setOnClickListener(v ->
                showToast("Библиотека обедов (заглушка)"));

        // Ужин
        view.findViewById(R.id.addCustomDishDinner).setOnClickListener(v -> openMyDishesScreen());
        view.findViewById(R.id.libraryDinner).setOnClickListener(v ->
                showToast("Библиотека ужинов (заглушка)"));
    }

    private void openMyDishesScreen() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentContainer, new MyDishesFragment())
                .addToBackStack(null)
                .commit();
    }

    private void initTrashButton(View view) {
        view.findViewById(R.id.trashButton).setOnClickListener(v ->
                showToast("Очистить меню (заглушка)"));
    }
    private void initSettingsButton(View view) {
        ImageButton settingsBtn = view.findViewById(R.id.settingsButton);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                if (getChildFragmentManager().findFragmentByTag(SettingsDialogFragment.TAG) != null) {
                    return;
                }
                // Создаем экземпляр диалога
                SettingsDialogFragment dialog = new SettingsDialogFragment();

                // Показываем его.
                // getChildFragmentManager() используется, если мы внутри фрагмента.
                // Если бы мы были в Activity, использовали бы getSupportFragmentManager().
                dialog.show(getChildFragmentManager(), SettingsDialogFragment.TAG);
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}