package com.faible.coplate;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.MealPlanApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.catalog.CatalogDishAdapter;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.model.MealPlanAddRequest;
import com.faible.coplate.util.DishJsonParser;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Catalog extends Fragment implements CatalogDishAdapter.Listener {

    private static final String[] MEAL_API_VALUES = {"breakfast", "lunch", "dinner"};

    private EditText searchInput;
    private RecyclerView recyclerView;
    private CatalogDishAdapter adapter;
    private DishApi dishApi;
    private MealPlanApi mealPlanApi;
    private String familyId;

    private final List<DishResponse> allDishes = new ArrayList<>();

    public Catalog() {
        super(R.layout.fragment_catalog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dishApi = RetrofitClient.getClient(requireContext()).create(DishApi.class);
        mealPlanApi = RetrofitClient.getClient(requireContext()).create(MealPlanApi.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        familyId = prefs.getString("family_id", null);

        searchInput = view.findViewById(R.id.searchInput);
        recyclerView = view.findViewById(R.id.catalogRecyclerView);
        initSettingsButton(view);

        adapter = new CatalogDishAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadLibrary();
    }

    private void loadLibrary() {
        dishApi.getLibraryDishes().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), R.string.catalog_load_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<DishResponse> parsed = DishJsonParser.parseFamilyDishes(response.body());
                allDishes.clear();
                for (DishResponse d : parsed) {
                    if (d == null) {
                        continue;
                    }
                    String src = d.getSource();
                    if (src == null || "library".equalsIgnoreCase(src.trim())) {
                        allDishes.add(d);
                    }
                }
                applyFilter(searchInput.getText() != null ? searchInput.getText().toString() : "");
                enrichCatalogWithDishIngredients(0);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            getString(R.string.network_error_simple) + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * В JSON списка библиотеки ингредиенты могут быть в поле ingredient или отсутствовать;
     * для каждого блюда без списка подгружаем полную карточку — там связанные строки DishIngredient.
     */
    private void enrichCatalogWithDishIngredients(int indexInAllDishes) {
        if (!isAdded()) {
            return;
        }
        if (indexInAllDishes >= allDishes.size()) {
            applyFilter(searchInput.getText() != null ? searchInput.getText().toString() : "");
            return;
        }
        DishResponse dish = allDishes.get(indexInAllDishes);
        Long dishId = parseDishId(dish);
        if (dishId == null || !dish.getIngredients().isEmpty()) {
            enrichCatalogWithDishIngredients(indexInAllDishes + 1);
            return;
        }

        dishApi.getDishById(dishId).enqueue(new Callback<DishResponse>() {
            @Override
            public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    dish.replaceIngredients(response.body().getIngredients());
                }
                enrichCatalogWithDishIngredients(indexInAllDishes + 1);
            }

            @Override
            public void onFailure(Call<DishResponse> call, Throwable t) {
                if (isAdded()) {
                    enrichCatalogWithDishIngredients(indexInAllDishes + 1);
                }
            }
        });
    }

    private void applyFilter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<DishResponse> filtered = new ArrayList<>();
        for (DishResponse d : allDishes) {
            String name = d.getName();
            if (q.isEmpty()) {
                filtered.add(d);
            } else if (name != null && name.toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(d);
            }
        }
        adapter.setItems(filtered);
    }

    @Override
    public void onDishTitleClick(@NonNull DishResponse dish) {
        Long id = parseDishId(dish);
        if (id == null) {
            Toast.makeText(requireContext(), R.string.catalog_dish_open_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String title = dish.getName();
        LibraryDishDetailDialog.newInstance(id, title)
                .show(getChildFragmentManager(), LibraryDishDetailDialog.TAG);
    }

    @Override
    public void onAddToPlanClick(@NonNull DishResponse dish) {
        Long id = parseDishId(dish);
        if (id == null) {
            Toast.makeText(requireContext(), R.string.catalog_dish_open_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (familyId == null || familyId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.family_required_for_plan, Toast.LENGTH_SHORT).show();
            return;
        }
        showAddToPlanDialog(id);
    }

    private void showAddToPlanDialog(long dishId) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_to_plan, null, false);
        Spinner daySpinner = form.findViewById(R.id.addPlanDaySpinner);
        Spinner mealSpinner = form.findViewById(R.id.addPlanMealSpinner);
        Button confirm = form.findViewById(R.id.addPlanConfirmButton);

        android.widget.ArrayAdapter<String> dayAdapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.plan_weekday_labels));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        String[] mealLabels = new String[]{
                getString(R.string.meal_breakfast),
                getString(R.string.meal_lunch),
                getString(R.string.meal_dinner)
        };
        android.widget.ArrayAdapter<String> mealAdapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                mealLabels);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealSpinner.setAdapter(mealAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_to_plan)
                .setView(form)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .create();

        confirm.setOnClickListener(v -> {
            int dayIndex = daySpinner.getSelectedItemPosition();
            int mealIndex = mealSpinner.getSelectedItemPosition();
            if (dayIndex < 0 || dayIndex > 6 || mealIndex < 0 || mealIndex > 2) {
                return;
            }
            int dayOfWeek = dayIndex + 1;
            String mealType = MEAL_API_VALUES[mealIndex];
            confirm.setEnabled(false);
            mealPlanApi.addDishToPlan(familyId, new MealPlanAddRequest(String.valueOf(dishId), dayOfWeek, mealType))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) {
                                return;
                            }
                            confirm.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), R.string.dish_added_to_meal, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(requireContext(), R.string.dish_add_to_plan_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (isAdded()) {
                                confirm.setEnabled(true);
                                Toast.makeText(requireContext(),
                                        getString(R.string.network_error_simple) + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        dialog.show();
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

    private void initSettingsButton(View view) {
        ImageButton settingsBtn = view.findViewById(R.id.settingsButton);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                if (getChildFragmentManager().findFragmentByTag(SettingsDialogFragment.TAG) != null) {
                    return;
                }
                SettingsDialogFragment dialog = new SettingsDialogFragment();
                dialog.show(getChildFragmentManager(), SettingsDialogFragment.TAG);
            });
        }
    }
}
