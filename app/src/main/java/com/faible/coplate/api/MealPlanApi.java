package com.faible.coplate.api;



import com.faible.coplate.model.MealPlanAddRequest;



import com.google.gson.JsonElement;



import retrofit2.Call;

import retrofit2.http.Body;

import retrofit2.http.DELETE;

import retrofit2.http.GET;

import retrofit2.http.POST;

import retrofit2.http.Path;



public interface MealPlanApi {



    @GET("api/families/{familyId}/meal-plans/week")

    Call<JsonElement> getWeekPlan(@Path("familyId") String familyId);



    @POST("api/families/{familyId}/meal-plans")

    Call<Void> addDishToPlan(

            @Path("familyId") String familyId,

            @Body MealPlanAddRequest request

    );



    /** Backend: DELETE /api/families/{id}/meal-plans/{planId} — удаление строки MealPlan по id. */

    @DELETE("api/families/{familyId}/meal-plans/{planId}")

    Call<Void> removeDishFromPlan(

            @Path("familyId") String familyId,

            @Path("planId") long planId

    );



    @DELETE("api/families/{familyId}/meal-plans/week")

    Call<Void> clearWeekPlan(@Path("familyId") String familyId);

}

