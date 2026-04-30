package com.faible.coplate.api;

import com.faible.coplate.model.User;
import com.faible.coplate.model.UpdateUserRequest; // Мы создадим эту модель ниже
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {

    // Получение данных текущего пользователя по ID
    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") String id);

    // Обновление данных пользователя (имя, логин, пароль)
    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") String id, @Body UpdateUserRequest request);
}