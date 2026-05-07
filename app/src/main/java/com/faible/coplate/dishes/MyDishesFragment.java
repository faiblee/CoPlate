package com.faible.coplate.dishes;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.DishCreateRequest;
import com.faible.coplate.model.DishIngredientRequest;
import com.faible.coplate.model.DishResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDishesFragment extends Fragment {

    private RecyclerView dishesRecyclerView;
    private LinearLayout addDishPanel;
    private Button addDishToggleButton;
    private Button saveDishButton;
    private EditText dishNameInput;
    private EditText dishIngredientsInput;
    private ImageButton backButton;

    private MyDishesAdapter adapter;
    private DishApi dishApi;
    private String familyId;
    private String userId;
    private boolean isSaving = false;

    public MyDishesFragment() {
        super(R.layout.fragment_my_dishes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dishApi = RetrofitClient.getClient(requireContext()).create(DishApi.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        familyId = prefs.getString("family_id", null);
        userId = prefs.getString("user_id", null);

        dishesRecyclerView = view.findViewById(R.id.myDishesRecyclerView);
        addDishPanel = view.findViewById(R.id.addDishPanel);
        addDishToggleButton = view.findViewById(R.id.addDishToggleButton);
        saveDishButton = view.findViewById(R.id.saveDishButton);
        dishNameInput = view.findViewById(R.id.dishNameInput);
        dishIngredientsInput = view.findViewById(R.id.dishIngredientsInput);
        backButton = view.findViewById(R.id.backButton);

        adapter = new MyDishesAdapter();
        dishesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishesRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        addDishToggleButton.setOnClickListener(v -> toggleAddPanel());
        saveDishButton.setOnClickListener(v -> saveDish());

        loadMyDishes();
    }

    private void toggleAddPanel() {
        boolean visible = addDishPanel.getVisibility() == View.VISIBLE;
        addDishPanel.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void loadMyDishes() {
        if (familyId == null || familyId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Не удалось определить пользователя или семью", Toast.LENGTH_SHORT).show();
            return;
        }
        dishApi.getFamilyDishes(familyId).enqueue(new Callback<List<DishResponse>>() {
            @Override
            public void onResponse(Call<List<DishResponse>> call, Response<List<DishResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DishResponse> myDishes = new ArrayList<>();
                    for (DishResponse dish : response.body()) {
                        if (userId.equals(dish.getOwnerId())) {
                            myDishes.add(dish);
                        }
                    }
                    adapter.setDishes(myDishes);
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить блюда", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DishResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDish() {
        String dishName = dishNameInput.getText().toString().trim();
        String ingredientsRaw = dishIngredientsInput.getText().toString().trim();
        if (dishName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название блюда", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ingredientsRaw.isEmpty()) {
            Toast.makeText(requireContext(), "Введите хотя бы один ингредиент", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSaving || familyId == null || userId == null) return;

        List<DishIngredientRequest> ingredients = parseIngredients(ingredientsRaw);
        if (ingredients.isEmpty()) {
            Toast.makeText(requireContext(), "Не удалось разобрать ингредиенты", Toast.LENGTH_SHORT).show();
            return;
        }

        isSaving = true;
        saveDishButton.setEnabled(false);
        DishCreateRequest request = new DishCreateRequest(
                dishName,
                "",
                "custom",
                familyId,
                userId,
                ingredients
        );

        dishApi.createCustomDish(request).enqueue(new Callback<DishResponse>() {
            @Override
            public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                isSaving = false;
                saveDishButton.setEnabled(true);
                if (response.isSuccessful()) {
                    dishNameInput.setText("");
                    dishIngredientsInput.setText("");
                    addDishPanel.setVisibility(View.GONE);
                    loadMyDishes();
                } else {
                    Toast.makeText(requireContext(), "Не удалось добавить блюдо", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DishResponse> call, Throwable t) {
                isSaving = false;
                saveDishButton.setEnabled(true);
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<DishIngredientRequest> parseIngredients(String raw) {
        List<DishIngredientRequest> ingredients = new ArrayList<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String ingredient = part.trim();
            if (!ingredient.isEmpty()) {
                ingredients.add(new DishIngredientRequest(ingredient, 1, "шт"));
            }
        }
        return ingredients;
    }
}
