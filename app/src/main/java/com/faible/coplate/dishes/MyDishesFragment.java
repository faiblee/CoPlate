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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.MealPlanApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.DishCreateRequest;
import com.faible.coplate.model.DishIngredientRequest;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.model.MealPlanAddRequest;
import com.faible.coplate.util.DishJsonParser;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDishesFragment extends Fragment implements MyDishesAdapter.MyDishesActionListener {

    public static final String ARG_MEAL_TYPE = "meal_type";
    public static final String ARG_DAY_OF_WEEK = "day_of_week";

    private RecyclerView dishesRecyclerView;
    private LinearLayout addDishPanel;
    private Button addDishToggleButton;
    private Button saveDishButton;
    private EditText dishNameInput;
    private EditText dishDescriptionInput;
    private EditText dishIngredientsInput;
    private ImageButton backButton;

    private MyDishesAdapter adapter;
    private DishApi dishApi;
    private MealPlanApi mealPlanApi;
    private String familyId;
    private String userId;
    private boolean isSaving = false;

    public MyDishesFragment() {
        super(R.layout.fragment_my_dishes);
    }


    public static MyDishesFragment newInstance(@Nullable String mealType, int dayOfWeek) {
        MyDishesFragment f = new MyDishesFragment();
        Bundle b = new Bundle();
        if (mealType != null) {
            b.putString(ARG_MEAL_TYPE, mealType);
        }
        b.putInt(ARG_DAY_OF_WEEK, dayOfWeek);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dishApi = RetrofitClient.getClient(requireContext()).create(DishApi.class);
        mealPlanApi = RetrofitClient.getClient(requireContext()).create(MealPlanApi.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        familyId = prefs.getString("family_id", null);
        userId = prefs.getString("user_id", null);

        dishesRecyclerView = view.findViewById(R.id.myDishesRecyclerView);
        addDishPanel = view.findViewById(R.id.addDishPanel);
        addDishToggleButton = view.findViewById(R.id.addDishToggleButton);
        saveDishButton = view.findViewById(R.id.saveDishButton);
        dishNameInput = view.findViewById(R.id.dishNameInput);
        dishDescriptionInput = view.findViewById(R.id.dishDescriptionInput);
        dishIngredientsInput = view.findViewById(R.id.dishIngredientsInput);
        backButton = view.findViewById(R.id.backButton);

        adapter = new MyDishesAdapter(this);
        dishesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishesRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        addDishToggleButton.setOnClickListener(v -> toggleAddPanel());
        saveDishButton.setOnClickListener(v -> saveDish());

        Bundle args = getArguments();
        boolean fromDayMealPlan = args != null
                && args.getString(ARG_MEAL_TYPE) != null
                && args.getInt(ARG_DAY_OF_WEEK, -1) >= 1
                && args.getInt(ARG_DAY_OF_WEEK, -1) <= 7;
        if (fromDayMealPlan) {
            addDishPanel.setVisibility(View.GONE);
        }

        loadMyDishes();
    }

    @Override
    public void onDishOpenForPlan(@NonNull DishResponse dish) {
        Bundle args = getArguments();
        String mealType = args != null ? args.getString(ARG_MEAL_TYPE) : null;
        int dow = args != null ? args.getInt(ARG_DAY_OF_WEEK, -1) : -1;
        if (mealType == null || dow < 1 || dow > 7) {
            Toast.makeText(requireContext(), R.string.need_meal_context_for_plan, Toast.LENGTH_SHORT).show();
            return;
        }
        String dishId = dish.getId();
        if (dishId == null || dishId.trim().isEmpty() || familyId == null || familyId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.dish_add_to_plan_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        mealPlanApi.addDishToPlan(familyId, new MealPlanAddRequest(dishId.trim(), dow, mealType)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.dish_added_to_meal, Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), R.string.dish_add_to_plan_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.network_error_simple) + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDeleteDish(@NonNull DishResponse dish) {
        Long id = parseDishId(dish);
        if (id == null) {
            Toast.makeText(requireContext(), R.string.dish_delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String title = dish.getName() != null && !dish.getName().trim().isEmpty()
                ? dish.getName().trim()
                : getString(R.string.my_dishes_title);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_dish_dialog_title)
                .setMessage(getString(R.string.delete_dish_confirm_message, title))
                .setNegativeButton("Отмена", null)
                .setPositiveButton(R.string.delete, (dialog, which) -> dishApi.deleteDish(id).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful()) {
                            adapter.removeDishById(dish.getId());
                            Toast.makeText(requireContext(), R.string.dish_deleted, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), R.string.dish_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), getString(R.string.network_error_simple) + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }))
                .show();
    }

    private void toggleAddPanel() {
        boolean visible = addDishPanel.getVisibility() == View.VISIBLE;
        addDishPanel.setVisibility(visible ? View.GONE : View.VISIBLE);
    }


    private boolean includeInMyDishes(DishResponse dish) {
        if (dish == null) {
            return false;
        }
        String rawSrc = dish.getSource();
        if (rawSrc == null || rawSrc.trim().isEmpty()) {
            return false;
        }
        if (!"custom".equalsIgnoreCase(rawSrc.trim())) {
            return false;
        }
        if (familyId == null || familyId.trim().isEmpty() || dish.getFamilyId() == null) {
            return true;
        }
        return familyId.trim().equals(dish.getFamilyId().trim());
    }

    private void loadMyDishes() {
        if (familyId == null || familyId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.family_required_for_plan, Toast.LENGTH_SHORT).show();
            return;
        }
        adapter.clearDishes();
        dishApi.getFamilyDishes(familyId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Не удалось загрузить блюда", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<DishResponse> liteList = DishJsonParser.parseFamilyDishes(response.body());
                List<DishResponse> customSummaries = new ArrayList<>();
                for (DishResponse lite : liteList) {
                    if (lite != null && lite.getSource() != null
                            && "custom".equalsIgnoreCase(lite.getSource().trim())) {
                        customSummaries.add(lite);
                    }
                }
                enrichCustomDishesOneByOne(customSummaries, 0);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enrichCustomDishesOneByOne(List<DishResponse> summaries, int index) {
        if (!isAdded()) {
            return;
        }
        if (index >= summaries.size()) {
            return;
        }
        Long id = parseDishId(summaries.get(index));
        if (id == null) {
            enrichCustomDishesOneByOne(summaries, index + 1);
            return;
        }
        dishApi.getDishById(id).enqueue(new Callback<DishResponse>() {
            @Override
            public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    DishResponse full = response.body();
                    if (includeInMyDishes(full)) {
                        adapter.appendDish(full);
                    }
                }
                enrichCustomDishesOneByOne(summaries, index + 1);
            }

            @Override
            public void onFailure(Call<DishResponse> call, Throwable t) {
                if (isAdded()) {
                    enrichCustomDishesOneByOne(summaries, index + 1);
                }
            }
        });
    }

    @Nullable
    private static Long parseDishId(DishResponse d) {
        if (d == null || d.getId() == null) {
            return null;
        }
        try {
            return Long.parseLong(d.getId().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void saveDish() {
        String dishName = dishNameInput.getText().toString().trim();
        String description = dishDescriptionInput.getText().toString().trim();
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
                description,
                "custom",
                familyId,
                userId,
                ingredients
        );

        dishApi.createCustomDish(request).enqueue(new Callback<DishResponse>() {
            @Override
            public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    isSaving = false;
                    saveDishButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Не удалось добавить блюдо", Toast.LENGTH_SHORT).show();
                    return;
                }

                isSaving = false;
                saveDishButton.setEnabled(true);
                dishNameInput.setText("");
                dishDescriptionInput.setText("");
                dishIngredientsInput.setText("");
                addDishPanel.setVisibility(View.GONE);
                loadMyDishes();
                Toast.makeText(requireContext(), R.string.dish_saved_to_my_dishes, Toast.LENGTH_LONG).show();
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
