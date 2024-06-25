package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem {
    private String id;
    private String denomination;

    @SerializedName("category")
    private Category category;

    @SerializedName("presentation")
    private Presentation presentation;

    @SerializedName("dispatch_area")
    private DispatchArea dispatchArea;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("price_pen")
    private double pricePen;

    private boolean active;
}
