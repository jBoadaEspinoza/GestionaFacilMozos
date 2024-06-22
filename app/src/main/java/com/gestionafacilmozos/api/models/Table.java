package com.gestionafacilmozos.api.models;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    private String id;
    private String denomination;
    private boolean occupied;
    @SerializedName("order_id_associated")
    private String orderIdAssocied;
}
