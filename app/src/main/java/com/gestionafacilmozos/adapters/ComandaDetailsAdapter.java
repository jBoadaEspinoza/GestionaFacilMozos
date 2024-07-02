package com.gestionafacilmozos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.databinding.ItemOrderDetailBinding;

import java.util.List;
import java.util.Locale;

public class ComandaDetailsAdapter extends RecyclerView.Adapter<ComandaDetailsAdapter.ViewHolder>{
    private Order comanda;
    private Context context;
    public ComandaDetailsAdapter(Order order, Context context) {
        this.comanda = order;
        this.context = context;
    }
    @NonNull
    @Override
    public ComandaDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailBinding itemOrderDetailBinding=ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(itemOrderDetailBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComandaDetailsAdapter.ViewHolder holder, int position) {
        List<OrderDetail> details = comanda.getDetails();
        OrderDetail item=details.get(position);
        loadImage(holder,item.getMenuItem());
        updateControls(holder,item);
        holder.binding.txtCategory.setText(item.getMenuItem().getCategory().getDenominationPerGroup());
        holder.binding.txtMenuItemDenomination.setText(item.getMenuItem().getDenomination());
        holder.binding.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity=item.getQuantity()+1;
                comanda.getDetails().get(position).setQuantity(quantity);
                OrderDetail newItem=comanda.getDetails().get(position);
                updateControls(holder,newItem);
            }
        });
        holder.binding.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity=item.getQuantity()-1;
                if(item.getQuantityReceivedForDispatchArea()==0){
                    if(quantity>0){
                        comanda.getDetails().get(position).setQuantity(quantity);
                        OrderDetail newItem=comanda.getDetails().get(position);
                        updateControls(holder,newItem);
                    }else{
                        Toast.makeText(context,"Se eliminara el item seleccionado",Toast.LENGTH_SHORT).show();
                        comanda.getDetails().remove(position);
                        notifyDataSetChanged();
                    }
                }else{
                    if(quantity>item.getQuantityReceivedForDispatchArea()){
                        comanda.getDetails().get(position).setQuantity(quantity);
                        OrderDetail newItem=comanda.getDetails().get(position);
                        updateControls(holder,newItem);
                    }else{
                        Toast.makeText(context,"No se puede quitar mas elementos porque ya han sido recibido por el area de despacho",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return comanda.getDetails().size();
    }
    private void updateControls(ViewHolder holder, OrderDetail item) {
        holder.binding.txtQuantity.setText(String.valueOf(item.getQuantity()));
        holder.binding.txtUnitPrice.setText(getAmountFormatted(item.getUnitPrice(),item.getQuantity()));
        if(item.getQuantity()==1){
            holder.binding.btnMinus.setImageResource(R.drawable.baseline_delete_outline_24);
        }else{
            holder.binding.btnMinus.setImageResource(R.drawable.baseline_remove_24);
        }
    }
    private void loadImage(@NonNull ComandaDetailsAdapter.ViewHolder holder,MenuItem menuItem){
        if (menuItem.getImageUrl() != null) {
            Glide.with(context)
                    .load(menuItem.getImageUrl())
                    .placeholder(R.drawable.image_not_found)
                    .centerCrop()
                    .into(holder.binding.imgMenuItem);
        } else {
            holder.binding.imgMenuItem.setImageResource(R.drawable.image_not_found);
        }
    }
    private String getAmountFormatted(double unitprice,int quantity){
        return "S/."+String.format(Locale.getDefault(), "%.2f", unitprice*quantity);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ItemOrderDetailBinding binding;
        public ViewHolder(ItemOrderDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
