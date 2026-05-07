package com.faible.coplate.api;

import com.faible.coplate.model.PurchaseCreateRequest;
import com.faible.coplate.model.PurchaseResponse;
import com.faible.coplate.model.PurchaseUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PurchaseApi {

    @GET("api/families/{familyId}/purchases")
    Call<List<PurchaseResponse>> getAllPurchases(@Path("familyId") String familyId);

    @POST("api/families/{familyId}/purchases")
    Call<PurchaseResponse> addPurchase(@Path("familyId") String familyId, @Body PurchaseCreateRequest request);

    @PUT("api/families/{familyId}/purchases/{purchaseId}")
    Call<PurchaseResponse> updatePurchase(
            @Path("familyId") String familyId,
            @Path("purchaseId") String purchaseId,
            @Body PurchaseUpdateRequest request
    );

    @PUT("api/families/{familyId}/purchases/{purchaseId}/bought")
    Call<PurchaseResponse> changeBoughtStatus(
            @Path("familyId") String familyId,
            @Path("purchaseId") String purchaseId
    );

    @DELETE("api/families/{familyId}/purchases/{purchaseId}")
    Call<Void> deletePurchase(
            @Path("familyId") String familyId,
            @Path("purchaseId") String purchaseId
    );
}
