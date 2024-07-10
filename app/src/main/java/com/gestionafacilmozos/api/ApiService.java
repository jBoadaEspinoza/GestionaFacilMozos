package com.gestionafacilmozos.api;

import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.requests.ComandaRequest;
import com.gestionafacilmozos.api.requests.LoginRequest;
import com.gestionafacilmozos.api.requests.ComandaDetailRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<Result> login(@Body LoginRequest loginRequest);
    @GET("users/me")
    Call<Result> getUserInfo(@Header("Authorization") String token);

    @GET("tables")
    Call<Result> getTables(@Header("Authorization") String token, @Query("limit") int limit, @Query("skip") int skip,@Query("sort") String sort);

    @GET("orders/{id}")
    Call<Result> getOrderById(@Header("Authorization") String token, @Path("id") String id);

    @GET("menu-items")
    Call<Result> getMenuItems(@Header("Authorization") String token, @Query("limit") int limit,@Query("skip") int skip,@Query("sort") String sort,@Query("denomination") String denomination);

    @POST("orders")
    Call<Result> createOrder(@Header("Authorization") String token, @Body ComandaRequest comandaRequest);

    @DELETE("orders/{id}")
    Call<Result> deleteOrder(@Header("Authorization") String token,@Path("id") String id);
    @PUT("order-details/{id}/quantity")
    Call<Result> updateQuantityOfOrderDetail(@Header("Authorization") String token,@Path("id") String id,@Query("q") int quantity);

    @POST("order-details")
    Call<Result> createOrderDetail(@Header("Authorization") String token, @Body ComandaDetailRequest comandaDetailRequest);

    @DELETE("order-details/{id}")
    Call<Result> deleteOrderDetail(@Header("Authorization") String token,@Path("id") String id);
}