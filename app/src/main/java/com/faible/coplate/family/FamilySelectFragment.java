package com.faible.coplate.family;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.faible.coplate.R;
import com.faible.coplate.SettingsDialogFragment;
import com.faible.coplate.api.FamilyApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.family.Family;
import com.faible.coplate.model.FamilyCreateRequest;
import com.faible.coplate.model.FamilyJoinRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FamilySelectFragment extends Fragment {

    private EditText etFamilyName, etInviteCode;
    private LinearLayout createContent, joinContent;
    private Button btnCreateToggle, btnJoinToggle, btnCreateSubmit, btnJoinSubmit;

    private FamilyApi familyApi;
    private String currentUserId;

    public FamilySelectFragment() {
        super(R.layout.fragment_family);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация View
        etFamilyName = view.findViewById(R.id.familyNameInput);
        etInviteCode = view.findViewById(R.id.familyCodeInput);
        createContent = view.findViewById(R.id.createFamilyContent);
        joinContent = view.findViewById(R.id.joinFamilyContent);
        btnCreateToggle = view.findViewById(R.id.createFamilyButton);
        btnJoinToggle = view.findViewById(R.id.joinFamilyButton);
        btnCreateSubmit = view.findViewById(R.id.createSubmitButton);
        btnJoinSubmit = view.findViewById(R.id.joinSubmitButton);

        // Скрываем панели при старте
        if (createContent != null) createContent.setVisibility(View.GONE);
        if (joinContent != null) joinContent.setVisibility(View.GONE);

        if (btnCreateToggle == null || createContent == null) {
            Toast.makeText(getContext(), "Ошибка XML: проверьте ID", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. API и Пользователь
        familyApi = RetrofitClient.getClient(requireContext()).create(FamilyApi.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Ошибка: нет пользователя", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Логика кнопок переключения (раскрыть/скрыть)
        btnCreateToggle.setOnClickListener(v -> {
            boolean isVisible = createContent.getVisibility() == View.VISIBLE;
            createContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            if (joinContent != null) joinContent.setVisibility(View.GONE);
        });

        if (btnJoinToggle != null && joinContent != null) {
            btnJoinToggle.setOnClickListener(v -> {
                boolean isVisible = joinContent.getVisibility() == View.VISIBLE;
                joinContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                if (createContent != null) createContent.setVisibility(View.GONE);
            });
        }

        // 4. Кнопка "Создать" (внутри панели)
        if (btnCreateSubmit != null) {
            btnCreateSubmit.setOnClickListener(v -> {
                String name = etFamilyName.getText().toString().trim();
                if (name.isEmpty()) {
                    etFamilyName.setError("Введите название");
                    return;
                }
                performCreateFamily(name);
            });
        }

        // 5. Кнопка "Присоединиться" (внутри панели)
        if (btnJoinSubmit != null) {
            btnJoinSubmit.setOnClickListener(v -> {
                String code = etInviteCode.getText().toString().trim();
                if (code.isEmpty()) {
                    etInviteCode.setError("Введите код");
                    return;
                }
                performJoinFamily(code);
            });
        }
        initSettingsButton(view);
    }

    private void performCreateFamily(String name) {
        btnCreateSubmit.setEnabled(false);
        FamilyCreateRequest request = new FamilyCreateRequest(name, currentUserId);

        familyApi.createFamily(request).enqueue(new Callback<Family>() {
            @Override
            public void onResponse(Call<Family> call, Response<Family> response) {
                btnCreateSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Family family = response.body();
                    saveFamilyData(family);
                    openFamilyScreen(); // ПЕРЕХОД
                } else {
                    showError(response, "Ошибка создания");
                }
            }

            @Override
            public void onFailure(Call<Family> call, Throwable t) {
                btnCreateSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Нет сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performJoinFamily(String code) {
        btnJoinSubmit.setEnabled(false);
        FamilyJoinRequest request = new FamilyJoinRequest(code, currentUserId);

        familyApi.joinFamily(request).enqueue(new Callback<Family>() {
            @Override
            public void onResponse(Call<Family> call, Response<Family> response) {
                btnJoinSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Family family = response.body();
                    saveFamilyData(family);
                    openFamilyScreen(); // ПЕРЕХОД
                } else {
                    showError(response, "Ошибка входа");
                }
            }

            @Override
            public void onFailure(Call<Family> call, Throwable t) {
                btnJoinSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Нет сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openFamilyScreen() {
        FamilyInsideFragment fragment = new FamilyInsideFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void saveFamilyData(Family family) {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        // Сохраняем под ПРОСТЫМИ ключами
        prefs.edit()
                .putString("family_id", family.getId())
                .putString("family_name", family.getName())
                .putString("family_invite_code", family.getInviteCode())
                .apply();
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

    private void showError(Response<Family> response, String defaultMsg) {
        String msg = defaultMsg;
        if (response.code() == 404) msg = "Не найдено";
        if (response.code() == 409) msg = "Уже состоите в семье";
        try {
            if (response.errorBody() != null) msg = response.errorBody().string();
        } catch (Exception e) {}
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }
}