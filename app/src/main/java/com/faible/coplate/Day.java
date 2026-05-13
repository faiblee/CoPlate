package com.faible.coplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.MealPlanApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.dishes.MyDishesFragment;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.model.MealPlanDishRef;
import com.faible.coplate.model.WeekDayMeals;
import com.faible.coplate.model.WeekMealPlanResponse;
import com.faible.coplate.util.IngredientTextFormatter;
import com.faible.coplate.util.WeekMealPlanJsonParser;

import com.google.gson.JsonElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Day extends Fragment {

    private static final String MEAL_BREAKFAST = "breakfast";
    private static final String MEAL_LUNCH = "lunch";
    private static final String MEAL_DINNER = "dinner";

    private RadioGroup dayGroup;
    private TextView selectedDayLabel;
    private LinearLayout breakfastDishesList;
    private LinearLayout lunchDishesList;
    private LinearLayout dinnerDishesList;

    private MealPlanApi mealPlanApi;
    private DishApi dishApi;
    private String familyId;
    private WeekMealPlanResponse cachedWeekPlan;
    private final Map<Long, DishResponse> dishDetailCache = new LinkedHashMap<>();
    private static final int DISH_CACHE_MAX_ENTRIES = 48;

    public Day() {
        super(R.layout.fragment_day);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mealPlanApi = RetrofitClient.getClient(requireContext()).create(MealPlanApi.class);
        dishApi = RetrofitClient.getClient(requireContext()).create(DishApi.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        familyId = prefs.getString("family_id", null);

        dayGroup = view.findViewById(R.id.dayGroup);
        selectedDayLabel = view.findViewById(R.id.selectedDayLabel);
        breakfastDishesList = view.findViewById(R.id.breakfastDishesList);
        lunchDishesList = view.findViewById(R.id.lunchDishesList);
        dinnerDishesList = view.findViewById(R.id.dinnerDishesList);

        initMealToggles(view);
        initActionButtons(view);
        initTrashButton(view);
        initSettingsButton(view);
        initDayGroupListener();
        updateDayTitle();

        PlannedDishDetailDialog.registerResultListener(this, this::loadWeekPlan);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWeekPlan();
    }

    private void initDayGroupListener() {
        dayGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateDayTitle();
            refreshMealListsFromCache();
        });
    }

    private void initMealToggles(View view) {
        Button breakfastBtn = view.findViewById(R.id.breakfastButton);
        Button lunchBtn = view.findViewById(R.id.lunchButton);
        Button dinnerBtn = view.findViewById(R.id.dinnerButton);

        LinearLayout breakfastActions = view.findViewById(R.id.breakfastActions);
        LinearLayout lunchActions = view.findViewById(R.id.lunchActions);
        LinearLayout dinnerActions = view.findViewById(R.id.dinnerActions);

        breakfastBtn.setOnClickListener(v -> toggleMealActions(breakfastActions, lunchActions, dinnerActions));
        lunchBtn.setOnClickListener(v -> toggleMealActions(lunchActions, breakfastActions, dinnerActions));
        dinnerBtn.setOnClickListener(v -> toggleMealActions(dinnerActions, breakfastActions, lunchActions));
    }

    private void toggleMealActions(LinearLayout show, LinearLayout... hide) {
        boolean isVisible = show.getVisibility() == View.VISIBLE;
        for (LinearLayout layout : hide) {
            layout.setVisibility(View.GONE);
        }
        show.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void initActionButtons(View view) {
        view.findViewById(R.id.addDishBreakfast).setOnClickListener(v -> showAddDishChooser(MEAL_BREAKFAST));
        view.findViewById(R.id.addDishLunch).setOnClickListener(v -> showAddDishChooser(MEAL_LUNCH));
        view.findViewById(R.id.addDishDinner).setOnClickListener(v -> showAddDishChooser(MEAL_DINNER));
    }

    private void showAddDishChooser(String mealType) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_dish_dialog_title)
                .setPositiveButton(R.string.add_custom_dish, (d, w) -> openMyDishesScreen(mealType))
                .setNegativeButton(R.string.library_dishes, (d, w) -> openLibraryTabFromDay())
                .show();
    }

    private void openLibraryTabFromDay() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openCatalogTab();
        }
    }

    private void openMyDishesScreen(String mealType) {
        int dow = getSelectedDayOfWeek();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentContainer, MyDishesFragment.newInstance(mealType, dow))
                .addToBackStack(null)
                .commit();
    }

    /**
     * День недели как в Postman: 1 = понедельник, …, 7 = воскресенье.
     */
    private int getSelectedDayOfWeek() {
        int checked = dayGroup.getCheckedRadioButtonId();
        if (checked == R.id.monday) return 1;
        if (checked == R.id.tuesday) return 2;
        if (checked == R.id.wednesday) return 3;
        if (checked == R.id.thursday) return 4;
        if (checked == R.id.friday) return 5;
        if (checked == R.id.saturday) return 6;
        if (checked == R.id.sunday) return 7;
        return 1;
    }

    private void updateDayTitle() {
        if (selectedDayLabel == null) return;
        int checked = dayGroup.getCheckedRadioButtonId();
        int nameRes = R.string.today_menu;
        if (checked == R.id.monday) nameRes = R.string.menu_for_monday;
        else if (checked == R.id.tuesday) nameRes = R.string.menu_for_tuesday;
        else if (checked == R.id.wednesday) nameRes = R.string.menu_for_wednesday;
        else if (checked == R.id.thursday) nameRes = R.string.menu_for_thursday;
        else if (checked == R.id.friday) nameRes = R.string.menu_for_friday;
        else if (checked == R.id.saturday) nameRes = R.string.menu_for_saturday;
        else if (checked == R.id.sunday) nameRes = R.string.menu_for_sunday;
        selectedDayLabel.setText(nameRes);
    }

    private void initTrashButton(View view) {
        view.findViewById(R.id.trashButton).setOnClickListener(v -> clearWeekMenu());
    }

    private void clearWeekMenu() {
        if (familyId == null || familyId.trim().isEmpty()) {
            showToast(getString(R.string.family_required_for_plan));
            return;
        }
        mealPlanApi.clearWeekPlan(familyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cachedWeekPlan = null;
                    clearDishContainers();
                    showToast(getString(R.string.week_menu_cleared));
                } else {
                    showToast(getString(R.string.could_not_clear_menu));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(getString(R.string.network_error_simple) + t.getMessage());
            }
        });
    }

    private void loadWeekPlan() {
        if (familyId == null || familyId.trim().isEmpty()) {
            cachedWeekPlan = null;
            clearDishContainers();
            return;
        }
        mealPlanApi.getWeekPlan(familyId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dishDetailCache.clear();
                    cachedWeekPlan = WeekMealPlanJsonParser.parse(response.body());
                    refreshMealListsFromCache();
                } else {
                    cachedWeekPlan = null;
                    clearDishContainers();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                cachedWeekPlan = null;
                clearDishContainers();
            }
        });
    }

    private void refreshMealListsFromCache() {
        if (breakfastDishesList == null) return;

        WeekDayMeals day = findDayForSelection(cachedWeekPlan, getSelectedDayOfWeek());
        if (day == null) {
            clearDishContainers();
            return;
        }
        fillDishContainer(breakfastDishesList, day.getBreakfast());
        fillDishContainer(lunchDishesList, day.getLunch());
        fillDishContainer(dinnerDishesList, day.getDinner());
    }

    @Nullable
    private WeekDayMeals findDayForSelection(@Nullable WeekMealPlanResponse plan, int dayOfWeek) {
        if (plan == null) return null;
        List<WeekDayMeals> days = plan.getDays();
        for (WeekDayMeals d : days) {
            if (d.getDayOfWeek() == dayOfWeek) {
                return d;
            }
        }
        for (WeekDayMeals d : days) {
            int db = d.getDayOfWeek();
            /* Воскресенье: 7 в UI или 0 с сервера */
            if ((dayOfWeek == 7 && db == 0) || (dayOfWeek == 0 && db == 7)) {
                return d;
            }
            /* Сервер может отдавать 0–6 (пн=0 … вс=6) */
            if (db >= 0 && db <= 6 && db + 1 == dayOfWeek) {
                return d;
            }
        }
        return null;
    }

    private void clearDishContainers() {
        if (breakfastDishesList != null) breakfastDishesList.removeAllViews();
        if (lunchDishesList != null) lunchDishesList.removeAllViews();
        if (dinnerDishesList != null) dinnerDishesList.removeAllViews();
    }

    private void fillDishContainer(@NonNull LinearLayout container, @NonNull List<MealPlanDishRef> dishes) {
        container.removeAllViews();
        if (getContext() == null || dishApi == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (MealPlanDishRef dish : dishes) {
            View row = inflater.inflate(R.layout.item_day_meal_plan_dish, container, false);
            TextView nameTv = row.findViewById(R.id.plannedDishName);
            TextView ingTv = row.findViewById(R.id.plannedDishIngredients);
            String nameRaw = dish.getName();
            final String label;
            if (nameRaw == null || nameRaw.trim().isEmpty()) {
                label = dish.getId() != null ? dish.getId() : "?";
            } else {
                label = nameRaw;
            }
            nameTv.setText(label);

            Long dishId = parseDishId(dish);
            Long planRowId = dish.getMealPlanRowId();
            long planRowArg = planRowId != null ? planRowId : 0L;
            if (dishId != null) {
                row.setOnClickListener(v ->
                        PlannedDishDetailDialog.newInstance(dishId, planRowArg, label)
                                .show(getChildFragmentManager(), PlannedDishDetailDialog.TAG));
            }

            if (dishId == null) {
                container.addView(row);
                continue;
            }
            DishResponse cached = dishDetailCache.get(dishId);
            if (cached != null) {
                bindPlannedIngredients(ingTv, cached);
                container.addView(row);
                continue;
            }
            ingTv.setVisibility(View.GONE);

            dishApi.getDishById(dishId).enqueue(new Callback<DishResponse>() {
                @Override
                public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                    if (!isAdded()) {
                        return;
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        putDishCache(dishId, response.body());
                        bindPlannedIngredients(ingTv, response.body());
                    }
                }

                @Override
                public void onFailure(Call<DishResponse> call, Throwable t) {
                    /* ингредиенты необязательны — название уже показано */
                }
            });
            container.addView(row);
        }
    }

    private void putDishCache(long dishId, DishResponse dish) {
        if (dishDetailCache.containsKey(dishId)) {
            dishDetailCache.remove(dishId);
        }
        dishDetailCache.put(dishId, dish);
        while (dishDetailCache.size() > DISH_CACHE_MAX_ENTRIES) {
            Long first = dishDetailCache.keySet().iterator().next();
            dishDetailCache.remove(first);
        }
    }

    @Nullable
    private static Long parseDishId(MealPlanDishRef ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        try {
            return Long.parseLong(ref.getId().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void bindPlannedIngredients(TextView ingredientView, DishResponse dish) {
        String line = IngredientTextFormatter.fromIngredients(dish.getIngredients());
        if (line.trim().isEmpty()) {
            ingredientView.setVisibility(View.GONE);
            return;
        }
        ingredientView.setText(line);
        ingredientView.setVisibility(View.VISIBLE);
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
