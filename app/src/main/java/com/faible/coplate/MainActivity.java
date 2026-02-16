package com.faible.coplate; // ЗАМЕНИТЕ НА ВАШ ПАКЕТ!

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private View breakfastActions, lunchActions, dinnerActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим контейнеры с кнопками действий
        breakfastActions = findViewById(R.id.breakfastActions);
        lunchActions = findViewById(R.id.lunchActions);
        dinnerActions = findViewById(R.id.dinnerActions);

        // Находим кнопки выбора блюда
        Button breakfastBtn = findViewById(R.id.breakfastButton);
        Button lunchBtn = findViewById(R.id.lunchButton);
        Button dinnerBtn = findViewById(R.id.dinnerButton);

        // Обработчик для завтрака
        breakfastBtn.setOnClickListener(v -> {
            hideAllActions();
            breakfastActions.setVisibility(View.VISIBLE);
        });

        // Обработчик для обеда
        lunchBtn.setOnClickListener(v -> {
            hideAllActions();
            lunchActions.setVisibility(View.VISIBLE);
        });

        // Обработчик для ужина
        dinnerBtn.setOnClickListener(v -> {
            hideAllActions();
            dinnerActions.setVisibility(View.VISIBLE);
        });
    }

    // Вспомогательный метод для скрытия всех контейнеров
    private void hideAllActions() {
        breakfastActions.setVisibility(View.GONE);
        lunchActions.setVisibility(View.GONE);
        dinnerActions.setVisibility(View.GONE);
    }
}