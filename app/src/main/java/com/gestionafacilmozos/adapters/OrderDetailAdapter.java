package com.gestionafacilmozos.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.databinding.ItemOrderDetailBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>{
    private List<OrderDetail> orderDetailList;
    private Context context;

    public OrderDetailAdapter() {
        this.orderDetailList = new ArrayList<>();
    }

    public OrderDetailAdapter(List<OrderDetail> orderDetailList,Context context) {
        this.orderDetailList = orderDetailList;
        this.context=context;
    }

    @NonNull
    @Override
    public OrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailBinding binding=ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailAdapter.ViewHolder holder, int position) {
        OrderDetail orderDetail=orderDetailList.get(position);

        MenuItem menuItem=orderDetail.getMenuItem();
        holder.binding.txtMenuItemDenomination.setText(menuItem.getDenomination());
        if(menuItem.getImageUrl()!=null){
            Glide.with(context)
                    .load(menuItem.getImageUrl())
                    .placeholder(R.drawable.image_not_found) // Imagen de placeholder opcional
                    .into(holder.binding.imgMenuItem);
        }

        holder.binding.txtQuantity.setText(String.valueOf(orderDetail.getQuantity()));
        double total=orderDetail.getQuantity()*orderDetail.getUnitPrice();
        String formattedTotal = String.format(Locale.getDefault(), "%.2f", total);
        holder.binding.txtUnitPrice.setText("S/."+formattedTotal);
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public void setOrderDetails(List<OrderDetail> details){
        this.orderDetailList=details;
        notifyDataSetChanged();
    }

    public List<OrderDetail> getCurrentOrderDetails(){
        return this.orderDetailList;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ItemOrderDetailBinding binding;

        public ViewHolder(ItemOrderDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
