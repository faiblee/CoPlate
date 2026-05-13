package com.faible.coplate.api;

import com.faible.coplate.model.DishCreateRequest;
import com.faible.coplate.model.DishResponse;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DishApi {

    @GET("api/families/{familyId}/dishes")
    Call<JsonElement> getFamilyDishes(@Path("familyId") String familyId);

    @POST("api/dishes/add_custom")
    Call<DishResponse> createCustomDish(@Body DishCreateRequest request);

    @GET("api/dishes/{id}")
    Call<DishResponse> getDishById(@Path("id") long id);

    @GET("api/library")
    Call<JsonElement> getLibraryDishes();

    @DELETE("api/dishes/{id}")
    Call<Void> deleteDish(@Path("id") long id);
}
