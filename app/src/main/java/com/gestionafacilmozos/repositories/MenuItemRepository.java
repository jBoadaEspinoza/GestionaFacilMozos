package com.gestionafacilmozos.repositories;

import com.gestionafacilmozos.api.ApiService;
import com.gestionafacilmozos.api.RetrofitClient;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.Result;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuItemRepository {
    private ApiService apiService;
    public MenuItemRepository(){
        this.apiService= RetrofitClient.getClient().create(ApiService.class);
    }
    public void get(String token,int limit,int skip,String sort,String denomination,ResultCallback.ListMenuItemData callback){
        Call<Result> call=apiService.getMenuItems(token,limit,skip,sort,denomination);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful() && response.body()!=null){
                    try {
                        JSONObject result= new JSONObject(response.body().getResponse());
                        boolean success=result.getBoolean("success");
                        if(success){
                            String data = result. getString("data");
                            Type listType=new TypeToken<List< MenuItem>>() {}.getType();
                            List<MenuItem> menuItemList=new Gson().fromJson(data,listType);
                            callback.onSuccess(menuItemList);
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
