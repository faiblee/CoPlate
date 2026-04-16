package com.faible.coplate;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;

import com.faible.coplate.authentication.AuthActivity;
import com.faible.coplate.family.FamilyInsideFragment;
import com.faible.coplate.family.FamilySelectFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout todayButton;
    private LinearLayout shoppingListButton;
    private LinearLayout familyButton;
    private LinearLayout libraryButton;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ПРОВЕРКА АВТОРИЗАЦИИ ПЕРЕД ЗАГРУЗКОЙ ИНТЕРФЕЙСА
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        String userId = prefs.getString("user_id", null);

        if (token == null || userId == null) {
            // Токена нет -> отправляем на экран логина
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            // Флаги очищают стек задач, чтобы при нажатии "Назад" из логина не вернуться в пустую MainActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Закрываем текущую активность
            return; // Прерываем выполнение метода, setContentView не вызывается
        }

        // 2. ЕСЛИ ТОКЕН ЕСТЬ -> загружаем интерфейс
        setContentView(R.layout.activity_main);

        // Регистрация обработчика кнопки "Назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Есть куда возвращаться → идём назад по фрагментам
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Нет истории → закрываем Activity (или можно показать диалог выхода)
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
        familyButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String familyId = prefs.getString("family_id", null);

            if (familyId != null) {
                // ЕСТЬ СЕМЬЯ -> Открываем экран семьи
                loadFragment(new FamilyInsideFragment(), true);
            } else {
                // НЕТ СЕМЬИ -> Открываем экран создания/входа
                loadFragment(new FamilySelectFragment(), true);
            }
        });
        libraryButton.setOnClickListener(v -> loadFragment(new Catalog(), true));
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
        } else if(fragmentClass == Catalog.class){
            libraryButton.setSelected(true);
        }else if (fragmentClass == FamilySelectFragment.class){
            familyButton.setSelected(true);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}