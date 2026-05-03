package com.faible.coplate;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;

import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.api.UserApi;
import com.faible.coplate.authentication.AuthActivity;
import com.faible.coplate.family.FamilyInsideFragment;
import com.faible.coplate.family.FamilySelectFragment;
import com.faible.coplate.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private LinearLayout todayButton;
    private LinearLayout shoppingListButton;
    private LinearLayout familyButton;
    private LinearLayout libraryButton;

    private Fragment currentFragment;
    private UserApi userApi;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ПРОВЕРКА АВТОРИЗАЦИИ ПЕРЕД ЗАГРУЗКОЙ ИНТЕРФЕЙСА
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        currentUserId = prefs.getString("user_id", null);

        if (token == null || currentUserId == null) {
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
        userApi = RetrofitClient.getClient(this).create(UserApi.class);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment visible = getSupportFragmentManager().findFragmentById(R.id.contentContainer);
            if (visible != null) {
                currentFragment = visible;
                updateNavigationSelection(visible.getClass());
            }
        });

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
        familyButton.setOnClickListener(v -> openFamilyTabWithMembershipCheck());
        libraryButton.setOnClickListener(v -> loadFragment(new Catalog(), true));
    }

    private void openFamilyTabWithMembershipCheck() {
        familyButton.setEnabled(false);
        userApi.getUserById(currentUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                familyButton.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    openFamilyByLocalCache();
                    return;
                }

                User user = response.body();
                String familyId = user.getFamilyId();
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

                if (familyId != null && !familyId.trim().isEmpty()) {
                    prefs.edit().putString("family_id", familyId).apply();
                    loadFragment(new FamilyInsideFragment(), true);
                } else {
                    prefs.edit()
                            .remove("family_id")
                            .remove("family_name")
                            .remove("family_invite_code")
                            .apply();
                    loadFragment(new FamilySelectFragment(), true);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                familyButton.setEnabled(true);
                openFamilyByLocalCache();
            }
        });
    }

    private void openFamilyByLocalCache() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String familyId = prefs.getString("family_id", null);
        if (familyId != null && !familyId.trim().isEmpty()) {
            loadFragment(new FamilyInsideFragment(), true);
        } else {
            loadFragment(new FamilySelectFragment(), true);
        }
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
        }else if (fragmentClass == FamilySelectFragment.class || fragmentClass == FamilyInsideFragment.class){
            familyButton.setSelected(true);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}