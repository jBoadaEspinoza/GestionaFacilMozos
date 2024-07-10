package com.gestionafacilmozos.repositories;


import android.content.Context;

import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class TableRepository {
    private ApiService apiService;
    private Context context;
    public TableRepository(Context context){
        this.context=context;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }
    public void get(String token,int limit,int skip,String sort,ResultCallback.ListTableData callback){
        Call<Result> call=apiService.getTables(token,limit,skip,sort);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body() != null){
                    try {
                        JSONObject result= new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            String data=result.getString("data");
                            Type listType=new TypeToken<List<Table>>() {}.getType();
                            List<Table> tableList=new Gson().fromJson(data,listType);
                            callback.onSuccess(tableList);
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
