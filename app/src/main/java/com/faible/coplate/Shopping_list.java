package com.faible.coplate;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Shopping_list extends Fragment {

    public Shopping_list() {
        super(R.layout.fragment_shopping_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Здесь позже будет логика списка покупок
    }
}