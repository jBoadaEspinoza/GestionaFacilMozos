package com.gestionafacilmozos.api.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String ruc;
    private String userName;
    private String password;
    private int roleId=1;

    public LoginRequest(String ruc,String userName,String password){
        this.ruc=ruc;
        this.userName=userName;
        this.password=password;
    }
}
