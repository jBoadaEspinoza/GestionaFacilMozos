package com.gestionafacilmozos.repositories;

import android.content.Context;

import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.requests.ComandaBitacoraRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class DispatchAreaRepository {
    private Context context;
    private ApiService apiService;
    public DispatchAreaRepository(Context context){
        this.context=context;
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
    }
    public void get(String token,String order_id,Long state,ResultCallback.DispatchArea callback){
        Call<Result> call=apiService.getDispatchAreaItems(token,order_id,state);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body()!=null) {
                    try {
                        JSONObject result = new JSONObject(response.body().getResponse());
                        boolean success = result.getBoolean("success");
                        if (success) {
                            Long num_items_registered=result.getLong("num_items_registered");
                            callback.onSuccess(num_items_registered);
                        }else{
                            String message=result.getString("message");
                            callback.onError(new ErrorResponse("server",message));
                        }
                    } catch (JSONException e) {
                        callback.onError(new ErrorResponse("system", e.getMessage()));
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
    public void sendAllItemsToDispatchArea(String token, ComandaBitacoraRequest comanda, ResultCallback.DispatchAreaAdd callback){
        Call<Result> call = this.apiService.sendAllItemsToDispatchArea(token,comanda);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    try {
                        JSONObject result = new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            Long num_items_added=result.getLong("num_items_added");
                            Long num_items_registered=result.getLong("num_items_registered");
                            callback.onSuccess(num_items_added,num_items_registered);
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
    public void deleteDispatchAreaByOrderDetailIdAndStateId(String token,String orderDetailId,Long stateId,int quantity,ResultCallback.DispatchAreaDelete callback){
        Call<Result> call=this.apiService.deleteDispatchAreaByOrderDetailAndState(token,orderDetailId,stateId,quantity);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body().getResponse()!=null){
                    try {
                        JSONObject result = new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            Long num_items_affected=result.getLong("num_items_affected");
                            callback.onSuccess(num_items_affected);
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
