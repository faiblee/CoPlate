package com.faible.coplate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private View breakfastActions;
    private View lunchActions;
    private View dinnerActions;
    private TextView selectedDayLabel;
    private TextView shoppingSuggestion;

    private Button breakfastButton;
    private Button lunchButton;
    private Button dinnerButton;

    private View selectedBottomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBottomNavigation();
        initActionButtons();

        setupMealToggleListeners();
        setupDaySelector();

        updateUiForDay(R.id.monday);
    }

    private void initViews() {
        breakfastActions = findViewById(R.id.breakfastActions);
        lunchActions = findViewById(R.id.lunchActions);
        dinnerActions = findViewById(R.id.dinnerActions);

        selectedDayLabel = findViewById(R.id.selectedDayLabel);
        shoppingSuggestion = findViewById(R.id.shoppingSuggestion);

        breakfastButton = findViewById(R.id.breakfastButton);
        lunchButton = findViewById(R.id.lunchButton);
        dinnerButton = findViewById(R.id.dinnerButton);
    }

    private void initBottomNavigation() {
        // Получаем все кнопки навигации
        View todayButton = findViewById(R.id.todayButton);
        View shoppingListButton = findViewById(R.id.shoppingListButton);
        View familyButton = findViewById(R.id.familyButton);
        View libraryButton = findViewById(R.id.libraryButton);

        // Список всех кнопок для удобного сброса выделения
        View[] navButtons = {todayButton, shoppingListButton, familyButton, libraryButton};

        // Общий слушатель для всех кнопок
        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сбросить выделение со всех кнопок
                for (View button : navButtons) {
                    if (button != null) {
                        button.setSelected(false);
                    }
                }
                // Выделить нажатую кнопку
                v.setSelected(true);
                selectedBottomButton = v;

                // Показать заглушку (ваша старая логика)
                String message = "";
                int id = v.getId();
                if (id == R.id.settingsButton) {
                    message = "Настройки пока в разработке";
                } else if (id == R.id.todayButton) {
                    message = "Раздел «Сегодня» уже открыт";
                } else if (id == R.id.shoppingListButton) {
                    message = "Откроем список покупок позже (заглушка)";
                } else if (id == R.id.familyButton) {
                    message = "Экран семьи будет подключён позже (заглушка)";
                } else if (id == R.id.libraryButton) {
                    message = "Библиотека блюд временно недоступна (заглушка)";
                }
                showToast(message);
            }
        };

        // Назначаем слушатель на все кнопки
        todayButton.setOnClickListener(navClickListener);
        shoppingListButton.setOnClickListener(navClickListener);
        familyButton.setOnClickListener(navClickListener);
        libraryButton.setOnClickListener(navClickListener);

        // Выделяем первую кнопку по умолчанию (Сегодня)
        todayButton.setSelected(true);
        selectedBottomButton = todayButton;
    }

    private void initActionButtons() {
        setupAddDishStub(R.id.addCustomDishBreakfast, "завтрака");
        setupAddDishStub(R.id.addCustomDishLunch, "обеда");
        setupAddDishStub(R.id.addCustomDishDinner, "ужина");

        setupStubClick(R.id.libraryBreakfast, "Открыть библиотеку для завтрака (заглушка)");
        setupStubClick(R.id.libraryLunch, "Открыть библиотеку для обеда (заглушка)");
        setupStubClick(R.id.libraryDinner, "Открыть библиотеку для ужина (заглушка)");
    }

    private void setupMealToggleListeners() {
        breakfastButton.setOnClickListener(v -> toggleMealActions(breakfastActions));
        lunchButton.setOnClickListener(v -> toggleMealActions(lunchActions));
        dinnerButton.setOnClickListener(v -> toggleMealActions(dinnerActions));
    }

    private void setupDaySelector() {
        RadioGroup dayGroup = findViewById(R.id.dayGroup);
        dayGroup.setOnCheckedChangeListener((group, checkedId) -> updateUiForDay(checkedId));
    }

    private void updateUiForDay(int checkedId) {
        if (checkedId == View.NO_ID) {
            return;
        }

        selectedDayLabel.setText(getString(R.string.today_menu));
        breakfastButton.setText(getString(R.string.meal_breakfast));
        lunchButton.setText(getString(R.string.meal_lunch));
        dinnerButton.setText(getString(R.string.meal_dinner));

        hideAllActions();
        shoppingSuggestion.setVisibility(View.GONE);
    }

    private void toggleMealActions(View mealActions) {
        boolean shouldShow = mealActions.getVisibility() != View.VISIBLE;
        hideAllActions();
        mealActions.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    private void setupAddDishStub(int buttonId, String mealName) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            shoppingSuggestion.setVisibility(View.VISIBLE);
            showToast("Добавление блюда для " + mealName + " (заглушка)");
        });
    }

    private void setupStubClick(int buttonId, String message) {
        View view = findViewById(buttonId);
        view.setOnClickListener(v -> showToast(message));
    }

    private void hideAllActions() {
        breakfastActions.setVisibility(View.GONE);
        lunchActions.setVisibility(View.GONE);
        dinnerActions.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}