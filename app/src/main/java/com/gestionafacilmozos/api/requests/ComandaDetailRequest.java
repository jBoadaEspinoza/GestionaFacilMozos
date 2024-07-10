package com.gestionafacilmozos.api.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComandaDetailRequest {
    private String order_id;
    private String menu_item_id;
    private double unit_price_pen;
    private int quantity;
    private String suggestion = null;
}
