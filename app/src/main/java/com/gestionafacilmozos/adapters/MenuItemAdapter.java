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
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.databinding.ItemMenuItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder>{
    private List<OrderDetail> comanda;
    private List<MenuItem> menuItemList;
    private Context context;
    public MenuItemAdapter(Context context, List<OrderDetail> comanda) {
        this.context = context;
        this.menuItemList=new ArrayList<>();
        this.comanda=comanda;
    }
    @NonNull
    @Override
    public MenuItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMenuItemBinding binding=ItemMenuItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }
    public void clearMenuItems() {
        menuItemList.clear();
        notifyDataSetChanged();
    }
    public List<MenuItem> getMenuItemList(){
        return this.menuItemList;
    }
    public void setMenuItemList(List<MenuItem> list){
        this.menuItemList=list;
        notifyDataSetChanged();
    }
    public void addMenuItem(List<MenuItem> newMenuItems) {
        int startPosition = menuItemList.size();
        menuItemList.addAll(newMenuItems);
        notifyItemRangeInserted(startPosition, newMenuItems.size());
    }
    @Override
    public void onBindViewHolder(@NonNull MenuItemAdapter.ViewHolder holder, int position) {
        MenuItem menuItem=menuItemList.get(position);
        onButtonsLoad(holder,menuItem);
        onImageLoad(holder,menuItem);
        holder.binding.txtCategory.setText(menuItem.getCategory().getDenominationPerGroup().toUpperCase());
        holder.binding.txtMenuItemDenomination.setText(menuItem.getDenomination());
        holder.binding.txtUnitPrice.setText("S/."+getUnitPriceFormatted(menuItem.getPricePen()));
        holder.binding.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetail dt=getDetailFromComanda(menuItem);
                int index=getIndex(menuItem).getAsInt();
                int quantityMin=dt.getQuantityReceivedForDispatchArea();
                int quantityCurrent=dt.getQuantity()-1;
                if(quantityCurrent>quantityMin){
                    comanda.get(index).setQuantity(quantityCurrent);
                }else{
                    if(quantityMin==0){
                        if(dt.getId()==null){
                            comanda.remove(index);
                        }else{
                            Toast.makeText(context,"Se eliminara desde la base de datos",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(context,"No se podra eliminar porque ya ha sido recepcionado por el area de despacho",Toast.LENGTH_SHORT).show();
                    }
                }
                onButtonsLoad(holder,menuItem);
            }
        });
        holder.binding.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAdded(menuItem,comanda)){
                    OrderDetail dt=getDetailFromComanda(menuItem);
                    int quantity=dt.getQuantity()+1;
                    int index=getIndex(menuItem).getAsInt();
                    comanda.get(index).setQuantity(quantity);
                    holder.binding.txtQuantity.setText(String.valueOf(quantity));
                }else{
                    OrderDetail dt=new OrderDetail(null,menuItem,1,menuItem.getPricePen(),0);
                    comanda.add(dt);
                }
                onButtonsLoad(holder,menuItem);
            }
        });
    }
    @Override
    public int getItemCount() {
        return menuItemList.size();
    }
    private void onImageLoad(MenuItemAdapter.ViewHolder holder,MenuItem menuItem){
        holder.binding.imgMenuItem.setImageDrawable(null);
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
    private void onButtonsLoad(MenuItemAdapter.ViewHolder holder,MenuItem menuItem){
        if(isAdded(menuItem,comanda)){
            holder.binding.btnMinus.setVisibility(View.VISIBLE);
            holder.binding.txtQuantity.setVisibility(View.VISIBLE);
            OrderDetail dt=getDetailFromComanda(menuItem);
            holder.binding.txtQuantity.setText(String.valueOf(dt.getQuantity()));
            if(dt.getQuantity()==1){
                holder.binding.btnMinus.setImageResource(R.drawable.baseline_delete_outline_24);
            }else{
                holder.binding.btnMinus.setImageResource(R.drawable.baseline_remove_24);
            }
        }else{
            int quantity=1;
            holder.binding.btnMinus.setVisibility(View.GONE);
            holder.binding.txtQuantity.setVisibility(View.GONE);
            holder.binding.txtQuantity.setText(String.valueOf(quantity));
        }
    }
    private String getUnitPriceFormatted(double unitPrice){
        return String.format(Locale.getDefault(), "%.2f", unitPrice);
    }
    private OrderDetail getDetailFromComanda(MenuItem menuItem){
        return comanda.stream()
                .filter(orderDetail -> orderDetail.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst()
                .orElse(null);
    }
    private OptionalInt getIndex(MenuItem menuItem){
        OptionalInt indexOpt = IntStream.range(0, comanda.size())
                .filter(i -> comanda.get(i).getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();
        return indexOpt;
    }
    private boolean isAdded(MenuItem menuItem, List<OrderDetail> comanda) {
        return comanda.stream().anyMatch(orderDetail -> orderDetail.getMenuItem().getId().equals(menuItem.getId()));
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemMenuItemBinding binding;
        public ViewHolder(ItemMenuItemBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
