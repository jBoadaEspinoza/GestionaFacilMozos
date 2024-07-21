package com.gestionafacilmozos.api.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComandaBitacoraRequest {
    private String order_id;
    private int state;
}
