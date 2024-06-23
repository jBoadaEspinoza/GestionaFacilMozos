package com.gestionafacilmozos.repositories;

import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private ApiService apiService;
    public OrderRepository(){
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
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
                            callback.onError(new ErrorResponse("server",message));
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
}
