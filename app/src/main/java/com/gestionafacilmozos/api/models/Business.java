package com.gestionafacilmozos.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Business {
    private String id;
    private String ruc;
    private String name;
    private BusinessType type;
}
