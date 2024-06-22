package com.gestionafacilmozos.api.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("response")
    private Object response;

    public String getResponse() {
        return new Gson().toJson(response);
    }


}
