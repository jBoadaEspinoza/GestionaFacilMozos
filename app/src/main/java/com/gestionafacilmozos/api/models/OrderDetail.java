package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    private String id;
    @SerializedName("menu_item_id")
    private String menuItemId;
    private int quantity;
    @SerializedName("unit_price")
    private double unitPrice;
}
