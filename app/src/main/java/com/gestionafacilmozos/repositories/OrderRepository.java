package com.gestionafacilmozos.repositories;

import android.content.Context;

import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.requests.ComandaRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class OrderRepository {
    private ApiService apiService;
    private Context context;
    public OrderRepository(Context context){
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
        this.context=context;
    }
    public void createOrder(String token, ComandaRequest comanda,ResultCallback.OrderCreate callback){
        Call<Result> call=this.apiService.createOrder(token,comanda);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body()!=null){
                    try {
                        JSONObject result=new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            String data=result.getString("data");
                            Order order=new Gson().fromJson(data,Order.class);
                            callback.onSuccess(order);
                        }else{
                            String message=result.getString("message");
                            String code=result.getString("code");
                            callback.onError(new ErrorResponse("server",code,message));
                        }
                    } catch (JSONException e) {
                        callback.onError(new ErrorResponse("system",e.getMessage()));
                    }
                }else{
                    callback.onError(new ErrorResponse("system",response.message()));
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                callback.onError(new ErrorResponse("system",t.getMessage()));
            }
        });
    }
    public void deleteOrder(String token,String id,ResultCallback.Result callback){
        Call<Result> call=apiService.deleteOrder(token,id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    JSONObject result= null;
                    try {
                        result = new JSONObject(response.body().getResponse());
                        boolean success= result.getBoolean("success");
                        if(success){
                            String message=result.getString("message");
                            callback.onSuccess(success,message);
                        }else{
                            String code=result.getString("code");
                            String message=result.getString("message");
                            callback.onError(new ErrorResponse("server",code,message));
                        }

                    } catch (JSONException e) {
                        callback.onError(new ErrorResponse("system",e.getMessage()));
                    }
                }else{
                    callback.onError(new ErrorResponse("system",response.message()));
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                if(t instanceof IOException){
                    callback.onError(new ErrorResponse("system", "no_internet_connection", context.getString(R.string.check_internet_connection)));
                }else if (t instanceof HttpException) {
                    int statusCode = ((HttpException) t).code();
                    callback.onError(new ErrorResponse("system",  t.getMessage()));
                } else {
                    callback.onError(new ErrorResponse("system", t.getMessage()));
                }
            }
        });
    }
    public void getOrderInfo(String token,String id,ResultCallback.OrderInfo callback){
        Call<Result> call=this.apiService.getOrderById(token,id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body()!=null){
                    try {
                        JSONObject result=new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            String data=result.getString("data");
                            Order order=new Gson().fromJson(data,Order.class);
                            callback.onSuccess(order);
                        }else{
                            String message=result.getString("message");
                            String code=result.getString("code");
                            callback.onError(new ErrorResponse("server",code,message));
                        }
                    } catch (JSONException e) {
                        callback.onError(new ErrorResponse("system",e.getMessage()));
                    }
                }else{
                    callback.onError(new ErrorResponse("system",response.message()));
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                if(t instanceof IOException){
                    callback.onError(new ErrorResponse("system", "no_internet_connection", context.getString(R.string.check_internet_connection)));
                }else if (t instanceof HttpException) {
                    int statusCode = ((HttpException) t).code();
                    callback.onError(new ErrorResponse("system",  t.getMessage()));
                } else {
                    callback.onError(new ErrorResponse("system", t.getMessage()));
                }
            }
        });
    }
}
