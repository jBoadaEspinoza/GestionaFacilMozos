package com.gestionafacilmozos.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    @SerializedName("issue_date")
    private String issueDate;
    @SerializedName("closing_date")
    private String closingDate;
    private boolean closing;
    private List<OrderDetail> details;
}
