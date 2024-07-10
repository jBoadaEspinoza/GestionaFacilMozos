package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    private String id=null;
    @SerializedName("menu_item")
    private MenuItem menuItem;
    private int quantity=0;
    @SerializedName("unit_price")
    private double unitPrice=0;
    @SerializedName("quantity_received_for_dispatch_area")
    private int quantityReceivedForDispatchArea;
}
