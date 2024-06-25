package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private String id;
    @SerializedName("denomination_per_unit")
    private String denominationPerUnit;
    @SerializedName("denomination_per_group")
    private String denominationPerGroup;
}
