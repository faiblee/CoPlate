package com.faible.coplate;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Применяет сохранённую тему (светлая / тёмная) до старта активностей.
 */
public class CoPlateApp extends Application {

    public static final String PREFS_NAME = "app_prefs";
    /** {@link AppCompatDelegate#MODE_NIGHT_NO}, {@link AppCompatDelegate#MODE_NIGHT_YES} */
    public static final String KEY_THEME_MODE = "theme_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        applyStoredTheme(this);
    }

    public static void applyStoredTheme(@NonNull Application app) {
        SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        if (mode != AppCompatDelegate.MODE_NIGHT_NO
                && mode != AppCompatDelegate.MODE_NIGHT_YES
                && mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void persistAndApplyTheme(@NonNull android.content.Context context, int mode) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_THEME_MODE, mode)
                .apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static int getStoredThemeMode(@NonNull android.content.Context context) {
        int mode = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        if (mode != AppCompatDelegate.MODE_NIGHT_NO
                && mode != AppCompatDelegate.MODE_NIGHT_YES
                && mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        return mode;
    }
}
