package com.gestionafacilmozos.repositories;

import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.requests.ComandaDetailRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailRepository {
    private ApiService apiService;
    public OrderDetailRepository(){
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
    }
    public void createOrderDetail(String token, ComandaDetailRequest detail,ResultCallback.OrderDetailCreate callback){
        Call<Result> call=this.apiService.createOrderDetail(token,detail);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    try {
                        JSONObject result=new JSONObject(response.body().getResponse());
                        boolean success= result.getBoolean("success");
                        if(success){
                            String data=result.getString("data");
                            OrderDetail orderDetail=new Gson().fromJson(data,OrderDetail.class);
                            callback.onSuccess(orderDetail);
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
    public void updateQuantityOfOrderDetail(String token, String id,int quantity,ResultCallback.Result callback){
        Call<Result> call=this.apiService.updateQuantityOfOrderDetail(token,id,quantity);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    try {
                        JSONObject result=new JSONObject(response.body().getResponse());
                        boolean success= result.getBoolean("success");
                        String message=result.getString("message");
                        callback.onSuccess(success,message);
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
    public void deleteOrderDetail(String token, String id,ResultCallback.Result callback){
        Call<Result> call=this.apiService.deleteOrderDetail(token,id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    try {
                        JSONObject result=new JSONObject(response.body().getResponse());
                        boolean success= result.getBoolean("success");
                        String message=result.getString("message");
                        callback.onSuccess(success,message);
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
