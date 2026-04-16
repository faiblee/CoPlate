package com.faible.coplate.family;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.api.FamilyApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.family.Family;
import com.faible.coplate.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FamilyInsideFragment extends Fragment {

    private TextView familyCodeText;
    private RecyclerView membersRecyclerView;
    private Button actionButton;
    private ImageButton settingsButton;

    private MemberAdapter memberAdapter;
    private FamilyApi familyApi;

    private String currentFamilyId;
    private String currentUserId;
    private String currentOwnerId;

    public FamilyInsideFragment() {
        super(R.layout.fragment_family_inside);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        familyApi = RetrofitClient.getClient(requireContext()).create(FamilyApi.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        currentFamilyId = prefs.getString("family_id_" + currentUserId, null);
        currentUserId = prefs.getString("user_id", null);

        if (currentFamilyId == null || currentUserId == null) {
            Toast.makeText(requireContext(), "Ошибка: данные семьи утеряны", Toast.LENGTH_LONG).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        // Инициализация UI
        familyCodeText = view.findViewById(R.id.familyCodeText);
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView);
        actionButton = view.findViewById(R.id.actionButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        // Настройка RecyclerView
        memberAdapter = new MemberAdapter(user ->
                Toast.makeText(requireContext(), "Участник: " + (user.getName() != null ? user.getName() : user.getUsername()), Toast.LENGTH_SHORT).show()
        );
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        membersRecyclerView.setAdapter(memberAdapter);

        // Загрузка данных
        loadFamilyInfo();
        loadMembers();

        // Кнопка настроек (заглушка)
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Настройки", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadFamilyInfo() {
        familyApi.getFamilyById(currentFamilyId).enqueue(new Callback<Family>() {
            @Override
            public void onResponse(Call<Family> call, Response<Family> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Family family = response.body();
                    if (familyCodeText != null) {
                        familyCodeText.setText(family.getInviteCode() != null ? family.getInviteCode() : "Нет кода");
                    }
                    currentOwnerId = family.getOwnerId();
                    setupActionButton();
                }
            }
            @Override
            public void onFailure(Call<Family> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка загрузки семьи", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMembers() {
        familyApi.getMembers(currentFamilyId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    memberAdapter.setMemberList(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка загрузки участников", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActionButton() {
        if (actionButton == null) return;

        if (currentUserId.equals(currentOwnerId)) {
            actionButton.setText("Распустить семью");
            actionButton.setTextColor(0xFFFF0000); // Красный
            actionButton.setOnClickListener(v -> deleteFamily());
        } else {
            actionButton.setText("Покинуть семью");
            actionButton.setTextColor(0xFF1F2A24); // Темный
            actionButton.setOnClickListener(v -> leaveFamily());
        }
    }

    private void deleteFamily() {
        // TODO: Вызов API удаления семьи (DELETE /api/families/{id})
        Toast.makeText(requireContext(), "Функция удаления (владелец)", Toast.LENGTH_SHORT).show();
    }

    private void leaveFamily() {
        // Очищаем локальные данные
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        prefs.edit()
                .remove("family_id")
                .remove("family_name")
                .remove("family_invite_code")
                .apply();

        Toast.makeText(requireContext(), "Вы вышли из семьи", Toast.LENGTH_SHORT).show();

        // Возврат на экран выбора
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}