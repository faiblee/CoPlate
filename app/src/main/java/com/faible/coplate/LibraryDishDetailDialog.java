package com.faible.coplate;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.faible.coplate.api.DishApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.util.IngredientTextFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryDishDetailDialog extends DialogFragment {

    public static final String TAG = "LibraryDishDetail";
    private static final String ARG_DISH_ID = "dishId";
    private static final String ARG_FALLBACK_TITLE = "fallbackTitle";

    @NonNull
    public static LibraryDishDetailDialog newInstance(long dishId, @Nullable String fallbackTitle) {
        LibraryDishDetailDialog d = new LibraryDishDetailDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_DISH_ID, dishId);
        b.putString(ARG_FALLBACK_TITLE, fallbackTitle);
        d.setArguments(b);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_dish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            dismissAllowingStateLoss();
            return;
        }
        long dishId = args.getLong(ARG_DISH_ID);
        String fallback = args.getString(ARG_FALLBACK_TITLE);

        TextView titleView = view.findViewById(R.id.libraryDetailTitle);
        TextView ingredientsView = view.findViewById(R.id.libraryDetailIngredients);
        TextView descriptionLabel = view.findViewById(R.id.libraryDetailDescriptionLabel);
        TextView descriptionView = view.findViewById(R.id.libraryDetailDescription);
        Button closeButton = view.findViewById(R.id.libraryDetailClose);

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
                    bindDetails(titleView, ingredientsView, descriptionLabel, descriptionView, response.body());
                } else {
                    titleView.setText(fallback != null ? fallback : "?");
                    ingredientsView.setText(getString(R.string.dish_no_ingredients));
                    descriptionLabel.setVisibility(View.VISIBLE);
                    descriptionView.setVisibility(View.VISIBLE);
                    descriptionView.setText(getString(R.string.dish_no_description));
                }
            }

            @Override
            public void onFailure(Call<DishResponse> call, Throwable t) {
                if (isAdded()) {
                    titleView.setText(fallback != null ? fallback : "?");
                    ingredientsView.setText(getString(R.string.network_error_simple) + t.getMessage());
                }
            }
        });

        closeButton.setOnClickListener(v -> dismiss());
    }

    private void bindDetails(
            TextView titleView,
            TextView ingredientsView,
            TextView descriptionLabel,
            TextView descriptionView,
            @NonNull DishResponse dish
    ) {
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
        descriptionLabel.setVisibility(View.VISIBLE);
        descriptionView.setVisibility(View.VISIBLE);
        if (desc != null && !desc.trim().isEmpty()) {
            descriptionView.setText(desc.trim());
        } else {
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
