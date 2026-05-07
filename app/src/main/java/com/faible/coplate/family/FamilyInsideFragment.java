package com.faible.coplate.family;

import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.faible.coplate.SettingsDialogFragment;
import com.faible.coplate.family.MemberAdapter;
import com.faible.coplate.api.FamilyApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.User;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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
    private boolean isOwner = false;
    private String inviteCode;

    public FamilyInsideFragment() {
        super(R.layout.fragment_family_inside);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        familyApi = RetrofitClient.getClient(requireContext()).create(FamilyApi.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
        currentFamilyId = prefs.getString("family_id", null);
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
        setupFamilyCodeCopy();

        // Настройка RecyclerView
        memberAdapter = new MemberAdapter(user ->
                Toast.makeText(requireContext(), "Участник: " + (user.getName() != null ? user.getName() : user.getUsername()), Toast.LENGTH_SHORT).show()
        );
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        membersRecyclerView.setAdapter(memberAdapter);
        memberAdapter.setCurrentFamilyId(currentFamilyId);

        // Загрузка данных
        loadFamilyInfo();

        // Кнопка настроек
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                SettingsDialogFragment dialog = new SettingsDialogFragment();
                dialog.show(getChildFragmentManager(), SettingsDialogFragment.TAG);
            });
        }
    }

    private void loadFamilyInfo() {
        familyApi.getFamilyById(currentFamilyId).enqueue(new Callback<Family>() {
            @Override
            public void onResponse(Call<Family> call, Response<Family> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Family family = response.body();
                    inviteCode = family.getInviteCode();
                    if (familyCodeText != null) {
                        familyCodeText.setText(inviteCode != null ? inviteCode : "Нет кода");
                    }
                    currentOwnerId = family.getOwnerId();
                    isOwner = currentUserId.equals(currentOwnerId);
                    setupActionButton();
                    loadMembers();
                }
            }
            @Override
            public void onFailure(Call<Family> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка загрузки семьи", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFamilyCodeCopy() {
        if (familyCodeText == null) return;
        familyCodeText.setOnClickListener(v -> {
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                Toast.makeText(requireContext(), "Код приглашения недоступен", Toast.LENGTH_SHORT).show();
                return;
            }

            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(requireContext().CLIPBOARD_SERVICE);
            if (clipboard == null) {
                Toast.makeText(requireContext(), "Не удалось скопировать код", Toast.LENGTH_SHORT).show();
                return;
            }

            ClipData clip = ClipData.newPlainText("Invite code", inviteCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Код приглашения скопирован", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMembers() {
        familyApi.getMembers(currentFamilyId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    memberAdapter.setMemberList(response.body());
                    // Передаем адаптеру информацию о владельце и текущем пользователе для кнопки исключения
                    memberAdapter.setCanKick(isOwner);
                    memberAdapter.setCurrentUserId(currentUserId);
                    memberAdapter.setOnKickListener(userId -> {
                        if (!isOwner && currentUserId.equals(userId)) {
                            leaveFamily();
                            return;
                        }
                        kickMember(userId);
                    });
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка загрузки участников", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void kickMember(String userId) {
        familyApi.kickMember(currentFamilyId, createKickBody(userId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Участник исключен", Toast.LENGTH_SHORT).show();
                    loadMembers(); // Перезагружаем список
                } else {
                    String errorMsg = "Ошибка исключения";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Нет сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupActionButton() {
        if (actionButton == null) return;

        if (currentUserId.equals(currentOwnerId)) {
            actionButton.setText("Распустить семью");
            actionButton.setTextColor(0xFFFFFFFF);
            actionButton.setOnClickListener(v -> deleteFamily());
        } else {
            actionButton.setText("Покинуть семью");
            actionButton.setTextColor(0xFFFFFFFF);
            actionButton.setOnClickListener(v -> leaveFamily());
        }
    }

    private void deleteFamily() {
        familyApi.deleteFamily(currentFamilyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Очищаем локальные данные
                    SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
                    prefs.edit()
                            .remove("family_id")
                            .remove("family_name")
                            .remove("family_invite_code")
                            .apply();

                    Toast.makeText(requireContext(), "Семья распущена", Toast.LENGTH_SHORT).show();

                    // Возврат на экран выбора
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    String errorMsg = "Ошибка удаления";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Нет сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void leaveFamily() {
        // Выход из семьи через kick с передачей своего ID
        familyApi.kickMember(currentFamilyId, createKickBody(currentUserId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Очищаем локальные данные
                    SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", requireContext().MODE_PRIVATE);
                    prefs.edit()
                            .remove("family_id")
                            .remove("family_name")
                            .remove("family_invite_code")
                            .apply();

                    Toast.makeText(requireContext(), "Вы вышли из семьи", Toast.LENGTH_SHORT).show();

                    // Надежно открываем экран выбора семьи после выхода
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contentContainer, new FamilySelectFragment())
                            .commit();
                } else {
                    String errorMsg = "Ошибка выхода";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Нет сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private RequestBody createKickBody(String userId) {
        MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
        return RequestBody.create(userId, jsonType);
    }
}