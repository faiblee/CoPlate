package com.faible.coplate.api;

import com.faible.coplate.family.Family;
import com.faible.coplate.model.FamilyCreateRequest;
import com.faible.coplate.model.FamilyJoinRequest;
import com.faible.coplate.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface FamilyApi {

    // Создание семьи
    @POST("api/families")
    Call<Family> createFamily(@Body FamilyCreateRequest request); // Тело: { "name": "...", "ownerId": "..." }

    // Получение семьи по ID
    @GET("api/families/{id}")
    Call<Family> getFamilyById(@Path("id") String id);

    // Присоединение к семье
    @POST("api/families/join")
    Call<Family> joinFamily(@Body FamilyJoinRequest request); // Тело: { "inviteCode": "...", "userId": "..." }

    // Получение списка участников
    @GET("api/families/{id}/members")
    Call<List<User>> getMembers(@Path("id") String id);

    // Получение кода приглашения (если он не приходит в основном объекте Family)
    @GET("api/families/{id}/invite_code")
    Call<String> getInviteCode(@Path("id") String id);

    // Удаление семьи (только владелец)
    @DELETE("api/families/{id}")
    Call<Void> deleteFamily(@Path("id") String id);

    // Исключение участника из семьи (только владелец)
    @PUT("api/families/{id}/kick")
    Call<List<String>> kickMember(@Path("id") String id, @Body String userId);

}