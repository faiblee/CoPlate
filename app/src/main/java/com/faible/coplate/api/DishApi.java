package com.faible.coplate.api;

import com.faible.coplate.model.DishCreateRequest;
import com.faible.coplate.model.DishResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DishApi {

    @GET("api/families/{familyId}/dishes")
    Call<List<DishResponse>> getFamilyDishes(@Path("familyId") String familyId);

    @POST("api/dishes/add_custom")
    Call<DishResponse> createCustomDish(@Body DishCreateRequest request);
}
