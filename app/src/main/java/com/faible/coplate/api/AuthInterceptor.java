package com.faible.coplate.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Получаем токен из SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        Request.Builder newRequestBuilder = originalRequest.newBuilder();

        // 1. Добавляем токен ТОЛЬКО если он есть
        if (token != null && !token.isEmpty()) {
            newRequestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        // 2. Добавляем Content-Type: application/json, только если в запросе есть тело
        // (чтобы не ломать GET-запросы без тела, хотя сервер обычно это игнорирует)
        if (originalRequest.body() != null) {
            // Проверяем, не установлен ли уже заголовок, чтобы не дублировать
            if (originalRequest.header("Content-Type") == null) {
                newRequestBuilder.addHeader("Content-Type", "application/json");
            }
        }

        return chain.proceed(newRequestBuilder.build());
    }
}