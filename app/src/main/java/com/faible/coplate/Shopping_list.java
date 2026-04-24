package com.faible.coplate;

import android.content.Context;
import android.os.Bundle;
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

import com.faible.coplate.shopping.ShoppingListAdapter;
import com.faible.coplate.shopping.ShoppingItem;

import java.util.ArrayList;
import java.util.List;

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

        // 2. Настройка RecyclerView
        itemList = new ArrayList<>();
        // Тестовые данные
        itemList.add(new ShoppingItem("Молоко"));
        itemList.add(new ShoppingItem("Хлеб"));

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
    }


    private void addNewItem() {
        String text = addItemInput.getText().toString().trim();
        if (!text.isEmpty()) {
            itemList.add(new ShoppingItem(text));
            adapter.notifyItemInserted(itemList.size() - 1);
            addItemInput.setText("");
            recyclerView.scrollToPosition(itemList.size() - 1);
        } else {
            Toast.makeText(getContext(), "Введите название товара", Toast.LENGTH_SHORT).show();
        }
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
        if (position >= 0 && position < itemList.size()) {
            ShoppingItem item = itemList.get(position);
            item.setChecked(!item.isChecked());
            adapter.notifyItemChanged(position);
        }
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
        if (itemList.isEmpty()) return;
        if (position >= 0 && position < itemList.size()) {
            ShoppingItem itemToRemove = itemList.get(position);

            // Удаляем
            itemList.remove(position);

            // Сообщаем адаптеру об удалении
            adapter.notifyItemRemoved(position);

            // Сообщаем об изменении диапазона после удаленного элемента (важно для корректной анимации)
            adapter.notifyItemRangeChanged(position, itemList.size());

            // Если список пуст, выходим из режима редактирования
            if (itemList.isEmpty()) {
                toggleEditMode(false);
                Toast.makeText(getContext(), "Список покупок пуст", Toast.LENGTH_SHORT).show();
            }
        }
    }
}