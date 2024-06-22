package com.gestionafacilmozos.repositories;

import android.util.Log;
import android.widget.Toast;

import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.models.User;
import com.gestionafacilmozos.api.requests.LoginRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private ApiService apiService;
    public UserRepository(){
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
    }
    public void getInfo(String token,ResultCallback.UserInfo callback){
        Call<Result> call=this.apiService.getUserInfo(token);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                    if(response.isSuccessful() && response.body() != null){
                        try{
                            JSONObject result = new JSONObject(response.body().getResponse());
                            boolean success = result.getBoolean("success");
                            if(success){
                                String data=result.getString("data");
                                Log.d("ErrorData",data);
                                User user=new Gson().fromJson(data,User.class);

                                callback.onSuccess(user);
                            }else{
                                String message=result.getString("message");
                                callback.onError(new ErrorResponse("server",message));
                            }
                        }catch(JSONException e){
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
    public void login(LoginRequest request, ResultCallback.Login callback){
        Call<Result> call=this.apiService.login(request);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body() != null){
                    try {
                        JSONObject result = new JSONObject(response.body().getResponse());
                        boolean success = result.getBoolean("success");
                        if(success){
                            String token=result.getString("token");
                            callback.onSuccess(token);
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
