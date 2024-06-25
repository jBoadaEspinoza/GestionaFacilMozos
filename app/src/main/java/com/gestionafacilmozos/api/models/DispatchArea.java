package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DispatchArea {
    private String id;
    @SerializedName("denominaiion_singular_es")
    private String denominationSingularEs;
    @SerializedName("denomination_plural_es")
    private String denominationPluralEs;
    private boolean active;
}
