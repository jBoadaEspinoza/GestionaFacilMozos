package com.gestionafacilmozos.repositories;

import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.models.User;
import com.gestionafacilmozos.api.responses.ErrorResponse;

import java.util.List;

public interface ResultCallback {
    interface Result{
        void onSuccess(boolean success,String message);
        void onError(ErrorResponse errorResponse);
    }
    interface Login{
        void onSuccess(String token);
        void onError(ErrorResponse errorResponse);
    }
    interface UserInfo{
        void onSuccess(User user);
        void onError(ErrorResponse errorResponse);
    }

    interface ListTableData{
        void onSuccess(List<Table> data);
        void onError(ErrorResponse errorResponse);
    }
    interface OrderInfo{
        void onSuccess(Order order);
        void onError(ErrorResponse errorResponse);
    }
    interface OrderCreate{
        void onSuccess(Order order);
        void onError(ErrorResponse errorResponse);
    }
    interface ListMenuItemData{
        void onSuccess(List<MenuItem> data);
        void onError(ErrorResponse errorResponse);
    }
    interface OrderDetailCreate{
        void onSuccess(OrderDetail orderDetail);
        void onError(ErrorResponse errorResponse);
    }
    interface DispatchArea{
        void onSuccess(Long num_items_registered);
        void onError(ErrorResponse errorResponse);
    }
    interface DispatchAreaAdd{
        void onSuccess(Long num_items_added,Long num_items_registered);
        void onError(ErrorResponse errorResponse);
    }
    interface DispatchAreaDelete{
        void onSuccess(Long num_items_affected);
        void onError(ErrorResponse errorResponse);
    }
}
