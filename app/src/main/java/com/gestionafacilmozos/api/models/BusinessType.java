package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessType {
    private String id;
    @SerializedName("denomination_single")
    private String denominationSingle;
    @SerializedName("denomination_plural")
    private String denominationPlural;
}
