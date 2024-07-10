package com.gestionafacilmozos.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String from;
    private String code=null;
    private String message;

    public ErrorResponse(String from, String message) {
        this.from = from;
        this.message = message;
    }
}
