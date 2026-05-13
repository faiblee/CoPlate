package com.faible.coplate;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.MealPlanApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.util.IngredientTextFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlannedDishDetailDialog extends DialogFragment {

    public static final String TAG = "PlannedDishDetail";
    private static final String ARG_DISH_ID = "dishId";
    private static final String ARG_PLAN_ROW_ID = "planRowId";
    private static final String ARG_FALLBACK_TITLE = "fallbackTitle";
    private static final String RESULT_KEY = "PlannedDishDetail_result";

    private TextView titleView;
    private TextView ingredientsView;
    private TextView descriptionLabel;
    private TextView descriptionView;
    private Button removeButton;
    private boolean removeInProgress;

    /**
     * @param mealPlanRowId id строки MealPlan из поля planId в ответе недели; неположительное значение — без кнопки удаления
     */
    public static PlannedDishDetailDialog newInstance(
            long dishId,
            long mealPlanRowId,
            @Nullable String fallbackTitle
    ) {
        PlannedDishDetailDialog d = new PlannedDishDetailDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_DISH_ID, dishId);
        b.putLong(ARG_PLAN_ROW_ID, mealPlanRowId);
        b.putString(ARG_FALLBACK_TITLE, fallbackTitle);
        d.setArguments(b);
        return d;
    }

    static void registerResultListener(
            @NonNull androidx.fragment.app.Fragment host,
            @NonNull Runnable onPlanChanged
    ) {
        host.requireActivity().getSupportFragmentManager().setFragmentResultListener(
                RESULT_KEY,
                host.getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    if (bundle.getBoolean("planChanged", false)) {
                        onPlanChanged.run();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planned_dish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            dismissAllowingStateLoss();
            return;
        }
        final long dishId = args.getLong(ARG_DISH_ID);
        final long planRowId = args.getLong(ARG_PLAN_ROW_ID);
        final String fallback = args.getString(ARG_FALLBACK_TITLE);

        titleView = view.findViewById(R.id.plannedDetailTitle);
        ingredientsView = view.findViewById(R.id.plannedDetailIngredients);
        descriptionLabel = view.findViewById(R.id.plannedDetailDescriptionLabel);
        descriptionView = view.findViewById(R.id.plannedDetailDescription);
        removeButton = view.findViewById(R.id.plannedDetailRemove);
        Button closeButton = view.findViewById(R.id.plannedDetailClose);

        if (planRowId <= 0) {
            removeButton.setVisibility(View.GONE);
        }

        titleView.setText(fallback != null && !fallback.trim().isEmpty() ? fallback : getString(R.string.loading_dish_details));
        ingredientsView.setText(getString(R.string.loading_dish_details));
        descriptionView.setVisibility(View.GONE);
        descriptionLabel.setVisibility(View.GONE);

        DishApi dishApi = RetrofitClient.getClient(requireContext()).create(DishApi.class);

        dishApi.getDishById(dishId).enqueue(new Callback<DishResponse>() {
            @Override
            public void onResponse(Call<DishResponse> call, Response<DishResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    bindDetails(response.body());
                } else {
                    titleView.setText(fallback != null ? fallback : "?");
                    ingredientsView.setText(getString(R.string.dish_no_ingredients));
                }
            }

            @Override
            public void onFailure(Call<DishResponse> call, Throwable t) {
                if (isAdded()) {
                    titleView.setText(fallback != null ? fallback : "?");
                    ingredientsView.setText(getString(R.string.dish_no_ingredients));
                }
            }
        });

        closeButton.setOnClickListener(v -> dismiss());

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String familyId = prefs.getString("family_id", null);

        removeButton.setOnClickListener(v -> {
            if (removeInProgress || planRowId <= 0) {
                return;
            }
            if (familyId == null || familyId.trim().isEmpty()) {
                Toast.makeText(requireContext(), R.string.family_required_for_plan, Toast.LENGTH_SHORT).show();
                return;
            }
            removeInProgress = true;
            removeButton.setEnabled(false);
            MealPlanApi mealPlanApi = RetrofitClient.getClient(requireContext()).create(MealPlanApi.class);
            mealPlanApi.removeDishFromPlan(familyId, planRowId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            removeInProgress = false;
                            if (!isAdded()) {
                                return;
                            }
                            removeButton.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), R.string.dish_removed_from_plan, Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager()
                                        .setFragmentResult(RESULT_KEY, bundleWithPlanChanged());
                                dismissAllowingStateLoss();
                            } else {
                                Toast.makeText(requireContext(), R.string.dish_remove_from_plan_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            removeInProgress = false;
                            if (isAdded()) {
                                removeButton.setEnabled(true);
                                Toast.makeText(requireContext(),
                                        getString(R.string.network_error_simple) + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private static Bundle bundleWithPlanChanged() {
        Bundle b = new Bundle();
        b.putBoolean("planChanged", true);
        return b;
    }

    private void bindDetails(@NonNull DishResponse dish) {
        String name = dish.getName();
        if (name != null && !name.trim().isEmpty()) {
            titleView.setText(name);
        }
        String ingLines = IngredientTextFormatter.ingredientsAsBulletLines(dish.getIngredients());
        if (ingLines.trim().isEmpty()) {
            ingredientsView.setText(getString(R.string.dish_no_ingredients));
        } else {
            ingredientsView.setText(ingLines);
        }

        String desc = dish.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            descriptionLabel.setVisibility(View.VISIBLE);
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(desc.trim());
        } else {
            descriptionLabel.setVisibility(View.VISIBLE);
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(getString(R.string.dish_no_description));
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (manager.findFragmentByTag(TAG) != null) {
            return;
        }
        super.show(manager, tag);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
