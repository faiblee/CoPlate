package com.faible.coplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.api.PurchaseApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.PurchaseCreateRequest;
import com.faible.coplate.model.PurchaseResponse;
import com.faible.coplate.shopping.ShoppingListAdapter;
import com.faible.coplate.shopping.ShoppingItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Shopping_list extends Fragment implements ShoppingListAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> itemList;

    private ImageButton editButton;
    private ImageButton settingsButton;
    private LinearLayout addPanel;
    private ImageButton addButton;
    private EditText addItemInput;

    private boolean isEditMode = false;
    private boolean isAddRequestInFlight = false;
    private PurchaseApi purchaseApi;
    private String currentFamilyId;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadPurchases(false);
            refreshHandler.postDelayed(this, 5000);
        }
    };

    public Shopping_list() {
        super(R.layout.fragment_shopping_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализация видов
        recyclerView = view.findViewById(R.id.shoppingListRecyclerView);
        editButton = view.findViewById(R.id.editButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        addPanel = view.findViewById(R.id.addPanel);
        addButton = view.findViewById(R.id.addButton);
        addItemInput = view.findViewById(R.id.addItemInput);
        purchaseApi = RetrofitClient.getClient(requireContext()).create(PurchaseApi.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        currentFamilyId = prefs.getString("family_id", null);
        if (currentFamilyId == null || currentFamilyId.trim().isEmpty()) {
            Toast.makeText(getContext(), "Вы не состоите в семье", Toast.LENGTH_SHORT).show();
        }

        // 2. Настройка RecyclerView
        itemList = new ArrayList<>();
        adapter = new ShoppingListAdapter(itemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 3. Логика кнопки редактирования
        editButton.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            toggleEditMode(isEditMode);
        });

        initSettingsButton(view);

        // 4. Логика кнопки добавления (Плюс)
        addButton.setOnClickListener(v -> {
            addNewItem();
        });

        // Обработка нажатия Enter в поле ввода
        addItemInput.setOnEditorActionListener((v, actionId, event) -> {
            addNewItem();
            return true;
        });

        loadPurchases(true);
    }


    private void addNewItem() {
        String text = addItemInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Введите название товара", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentFamilyId == null || purchaseApi == null || isAddRequestInFlight) return;
        isAddRequestInFlight = true;
        addButton.setEnabled(false);

        PurchaseCreateRequest request = new PurchaseCreateRequest(text, 1, "шт", "manual", null);
        purchaseApi.addPurchase(currentFamilyId, request).enqueue(new Callback<PurchaseResponse>() {
            @Override
            public void onResponse(Call<PurchaseResponse> call, Response<PurchaseResponse> response) {
                isAddRequestInFlight = false;
                addButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    addItemInput.setText("");
                    loadPurchases(false);
                } else {
                    Toast.makeText(getContext(), "Не удалось добавить товар", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                isAddRequestInFlight = false;
                addButton.setEnabled(true);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditMode(boolean enabled) {
        isEditMode = enabled;
        if (enabled) {
            addPanel.setVisibility(View.VISIBLE);
            adapter.setEditMode(true);

            // Отложенный фокус и показ клавиатуры
            addItemInput.postDelayed(() -> {
                if (getContext() != null) {
                    addItemInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(addItemInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 200);
        } else {
            addPanel.setVisibility(View.GONE);
            adapter.setEditMode(false);

            // Скрыть клавиатуру
            if (getContext() != null && getView() != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            }
        }
    }

    // Реализация интерфейса кликов из адаптера
    @Override
    public void onCheckClick(int position) {
        if (position < 0 || position >= itemList.size() || currentFamilyId == null || purchaseApi == null) return;

        ShoppingItem item = itemList.get(position);
        if (item.getId() == null || item.getId().trim().isEmpty()) return;

        purchaseApi.changeBoughtStatus(currentFamilyId, item.getId()).enqueue(new Callback<PurchaseResponse>() {
            @Override
            public void onResponse(Call<PurchaseResponse> call, Response<PurchaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.set(position, mapPurchase(response.body()));
                    adapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(getContext(), "Не удалось обновить статус", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onDeleteClick(int position) {
        if (itemList.isEmpty() || position < 0 || position >= itemList.size() || currentFamilyId == null || purchaseApi == null) return;

        ShoppingItem itemToRemove = itemList.get(position);
        if (itemToRemove.getId() == null || itemToRemove.getId().trim().isEmpty()) return;

        purchaseApi.deletePurchase(currentFamilyId, itemToRemove.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    itemList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, itemList.size());
                    if (itemList.isEmpty()) {
                        toggleEditMode(false);
                        Toast.makeText(getContext(), "Список покупок пуст", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Не удалось удалить товар", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void loadPurchases(boolean showError) {
        if (currentFamilyId == null || purchaseApi == null) return;
        purchaseApi.getAllPurchases(currentFamilyId).enqueue(new Callback<List<PurchaseResponse>>() {
            @Override
            public void onResponse(Call<List<PurchaseResponse>> call, Response<List<PurchaseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShoppingItem> mappedItems = new ArrayList<>();
                    for (PurchaseResponse purchase : response.body()) {
                        mappedItems.add(mapPurchase(purchase));
                    }
                    itemList.clear();
                    itemList.addAll(mappedItems);
                    adapter.notifyDataSetChanged();
                } else if (showError) {
                    Toast.makeText(getContext(), "Не удалось загрузить список покупок", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PurchaseResponse>> call, Throwable t) {
                if (showError) {
                    Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ShoppingItem mapPurchase(PurchaseResponse purchase) {
        return new ShoppingItem(
                purchase.getId(),
                purchase.getName(),
                purchase.isBought(),
                purchase.getQuantity(),
                purchase.getUnit()
        );
    }
}