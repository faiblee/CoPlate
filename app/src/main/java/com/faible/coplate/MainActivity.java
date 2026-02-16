package com.faible.coplate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private View breakfastActions;
    private View lunchActions;
    private View dinnerActions;
    private TextView selectedDayLabel;
    private TextView shoppingSuggestion;

    private Button breakfastButton;
    private Button lunchButton;
    private Button dinnerButton;

    private final Map<Integer, DayMenuStub> dayMenus = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDayMenuStubs();
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

    private void initDayMenuStubs() {
        dayMenus.put(R.id.monday, new DayMenuStub("Понедельник", "Завтрак · Овсянка с ягодами", "Обед · Куриный суп", "Ужин · Гречка с овощами"));
        dayMenus.put(R.id.tuesday, new DayMenuStub("Вторник", "Завтрак · Тост с авокадо", "Обед · Паста с индейкой", "Ужин · Рыба с рисом"));
        dayMenus.put(R.id.wednesday, new DayMenuStub("Среда", "Завтрак · Сырники", "Обед · Рис и тефтели", "Ужин · Овощной салат и омлет"));
        dayMenus.put(R.id.thursday, new DayMenuStub("Четверг", "Завтрак · Йогурт и гранола", "Обед · Борщ", "Ужин · Тушёные овощи"));
        dayMenus.put(R.id.friday, new DayMenuStub("Пятница", "Завтрак · Блины", "Обед · Говядина с картофелем", "Ужин · Киноа с курицей"));
        dayMenus.put(R.id.saturday, new DayMenuStub("Суббота", "Завтрак · Яичница", "Обед · Лазанья", "Ужин · Крем-суп"));
        dayMenus.put(R.id.sunday, new DayMenuStub("Воскресенье", "Завтрак · Творог и фрукты", "Обед · Плов", "Ужин · Запечённая рыба"));
    }

    private void initBottomNavigation() {
        setupStubClick(R.id.settingsButton, "Настройки пока в разработке");
        setupStubClick(R.id.todayButton, "Раздел «Сегодня» уже открыт");
        setupStubClick(R.id.shoppingListButton, "Откроем список покупок позже (заглушка)");
        setupStubClick(R.id.familyButton, "Экран семьи будет подключён позже (заглушка)");
        setupStubClick(R.id.libraryButton, "Библиотека блюд временно недоступна (заглушка)");
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
        DayMenuStub stub = dayMenus.get(checkedId);
        if (stub == null) {
            return;
        }

        selectedDayLabel.setText(String.format(Locale.getDefault(), "Меню на %s", stub.dayName));
        breakfastButton.setText(stub.breakfast);
        lunchButton.setText(stub.lunch);
        dinnerButton.setText(stub.dinner);

        hideAllActions();
        breakfastActions.setVisibility(View.VISIBLE);

        shoppingSuggestion.setVisibility(View.GONE);

        RadioButton selectedDay = findViewById(checkedId);
        showToast("Выбран день: " + selectedDay.getText());
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

    private static class DayMenuStub {
        final String dayName;
        final String breakfast;
        final String lunch;
        final String dinner;

        DayMenuStub(String dayName, String breakfast, String lunch, String dinner) {
            this.dayName = dayName;
            this.breakfast = breakfast;
            this.lunch = lunch;
            this.dinner = dinner;
        }
    }
}