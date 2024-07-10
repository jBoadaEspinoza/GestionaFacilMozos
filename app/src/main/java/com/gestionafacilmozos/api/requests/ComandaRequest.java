package com.gestionafacilmozos.api.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComandaRequest {
    private String customer_id=null;
    private String table_id=null;

    public ComandaRequest(String table_id) {
        this.table_id = table_id;
    }
}
