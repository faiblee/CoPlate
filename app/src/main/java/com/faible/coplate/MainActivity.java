package com.faible.coplate;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout todayButton;
    private LinearLayout shoppingListButton;
    private LinearLayout familyButton;
    private LinearLayout libraryButton;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Регистрация обработчика кнопки "Назад" (новый способ)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Есть куда возвращаться → идём назад по фрагментам
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Нет истории → закрываем Activity
                    finish();
                }
            }
        });

        initBottomNavigation();

        // При первом запуске показываем экран "День"
        if (savedInstanceState == null) {
            loadFragment(new Day(), false);
        }
    }

    private void initBottomNavigation() {
        todayButton = findViewById(R.id.todayButton);
        shoppingListButton = findViewById(R.id.shoppingListButton);
        familyButton = findViewById(R.id.familyButton);
        libraryButton = findViewById(R.id.libraryButton);

        todayButton.setOnClickListener(v -> loadFragment(new Day(), true));
        shoppingListButton.setOnClickListener(v -> loadFragment(new Shopping_list(), true));
        familyButton.setOnClickListener(v -> showToast("Экран семьи в разработке"));
        libraryButton.setOnClickListener(v -> showToast("Библиотека в разработке"));
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        // Не перезагружать, если тот же фрагмент уже активен
        if (currentFragment != null &&
                currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        var transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, fragment);

        if (addToBackStack && currentFragment != null) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        currentFragment = fragment;

        updateNavigationSelection(fragment.getClass());
    }

    private void updateNavigationSelection(Class<?> fragmentClass) {
        todayButton.setSelected(false);
        shoppingListButton.setSelected(false);
        familyButton.setSelected(false);
        libraryButton.setSelected(false);

        if (fragmentClass == Day.class) {
            todayButton.setSelected(true);
        } else if (fragmentClass == Shopping_list.class) {
            shoppingListButton.setSelected(true);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}