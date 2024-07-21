package com.gestionafacilmozos.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.requests.ComandaDetailRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.ItemMenuItemBinding;
import com.gestionafacilmozos.repositories.OrderDetailRepository;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.utilities.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder>{
    private OrderDetailRepository orderDetailRepository;
    private Order comanda;
    private List<MenuItem> menuItemList;
    private Context context;
    public MenuItemAdapter(Context context, Order comanda) {
        this.context = context;
        this.menuItemList=new ArrayList<>();
        this.comanda=comanda;
        this.orderDetailRepository=new OrderDetailRepository();
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
        OrderDetail dt=getOrderDetailAdded(menuItem).orElse(null);
        int quantity=0;
        if(dt!=null){
            quantity=dt.getQuantity();
        }
        holder.binding.txtCategory.setText(menuItem.getCategory().getDenominationPerGroup().toUpperCase());
        holder.binding.txtMenuItemDenomination.setText(menuItem.getDenomination());
        onImageLoad(holder,menuItem);
        updateControls(holder,menuItem,quantity);
        holder.binding.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetail detail=getOrderDetailAdded(menuItem).orElse(null);
                int quantity=detail.getQuantity()-1;
                if(quantity>0){
                    if((quantity+1)>detail.getQuantityReceivedForDispatchArea()){
                        updateQuantity(holder,detail,quantity);
                    }else{
                        alertMessage(menuItem);
                    }
                }else{
                    showExitConfirmationDialog(holder,menuItem);
                }
            }
        });
        holder.binding.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetail detail=getOrderDetailAdded(menuItem).orElse(null);
                if(detail!=null){
                    int quantity=detail.getQuantity()+1;
                    updateQuantity(holder,detail,quantity);
                }else{
                    addNewOrderDetail(holder,menuItem);
                }
            }
        });
    }

    private void alertMessage(MenuItem menuItem) {
        new AlertDialog.Builder(context)
                .setTitle("No puedes eliminar el registro:")
                .setMessage("El registro "+menuItem.getDenomination()+" se encuentra en area de despacho")
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void addNewOrderDetail(ViewHolder holder, MenuItem menuItem) {
        int quantity=1;
        ComandaDetailRequest input=new ComandaDetailRequest(comanda.getId(), menuItem.getId(), menuItem.getPricePen(), quantity,"");
        LoadingDialogFragment loading = new LoadingDialogFragment("Actualizando registro...");
        loading.show(((MainActivity) context).getSupportFragmentManager(), "loading");
        orderDetailRepository.createOrderDetail(MainActivity.getToken(), input, new ResultCallback.OrderDetailCreate() {
            @Override
            public void onSuccess(OrderDetail orderDetail) {
                loading.dismiss();
                comanda.getDetails().add(orderDetail);
                notifyDataSetChanged();
                updateControls(holder,menuItem,quantity);
            }
            @Override
            public void onError(ErrorResponse errorResponse) {}
        });
    }

    private void updateQuantity(ViewHolder holder, OrderDetail detail, int quantity) {
        LoadingDialogFragment loading = new LoadingDialogFragment("Actualizando registro...");
        loading.show(((MainActivity) context).getSupportFragmentManager(), "loading");
        orderDetailRepository.updateQuantityOfOrderDetail(MainActivity.getToken(), detail.getId(), quantity, new ResultCallback.Result() {
            @Override
            public void onSuccess(boolean success, String message) {
                loading.dismiss();
                int index=getOrderDetailIndex(detail.getMenuItem()).orElse(-1);
                comanda.getDetails().get(index).setQuantity(quantity);
                notifyDataSetChanged();
                updateControls(holder,detail.getMenuItem(),quantity);
            }
            @Override
            public void onError(ErrorResponse errorResponse) {}
        });
    }

    private void showExitConfirmationDialog(ViewHolder holder, MenuItem menuItem) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar acción:")
                .setMessage("¿Esta seguro que desea eliminar "+menuItem.getDenomination()+"?")
                .setPositiveButton("Si", (dialog, which) -> removeMenuItem(holder,menuItem))
                .setNegativeButton("No", null)
                .show();
    }

    private void removeMenuItem(ViewHolder holder, MenuItem menuItem) {
        int index=getOrderDetailIndex(menuItem).orElse(-1);
        OrderDetail orderDetailToRemove=comanda.getDetails().get(index);
        LoadingDialogFragment loading = new LoadingDialogFragment("Eliminando registro...");
        loading.show(((MainActivity) context).getSupportFragmentManager(), "loading");
        orderDetailRepository.deleteOrderDetail(MainActivity.getToken(), orderDetailToRemove.getId(), new ResultCallback.Result() {
            @Override
            public void onSuccess(boolean success, String message) {
                loading.dismiss();
                comanda.getDetails().remove(index);
                notifyDataSetChanged();
                updateControls(holder,menuItem,0);
            }
            @Override
            public void onError(ErrorResponse errorResponse) {

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
    private Optional<OrderDetail> getOrderDetailAdded(MenuItem item){
        return comanda.getDetails().stream()
                .filter(dt->dt.getMenuItem().getId().equals(item.getId()))
                .findFirst();
    }
    private Optional<Integer> getOrderDetailIndex(MenuItem item) {
        return Optional.of(comanda.getDetails().stream()
                .map(OrderDetail::getMenuItem)
                .map(MenuItem::getId)
                .collect(Collectors.toList())
                .indexOf(item.getId()));
    }
    private void updateControls(MenuItemAdapter.ViewHolder holder, MenuItem item,int quantity) {
        holder.binding.txtQuantity.setText(String.valueOf(quantity));
        holder.binding.txtUnitPrice.setText(getPriceFormatted(item.getPricePen()));
        if(quantity==1){
            holder.binding.btnMinus.setImageResource(R.drawable.baseline_delete_outline_24);
            holder.binding.btnMinus.setVisibility(View.VISIBLE);
            holder.binding.txtQuantity.setVisibility(View.VISIBLE);
        }else{
            if(quantity==0){
                holder.binding.btnMinus.setVisibility(View.GONE);
                holder.binding.txtQuantity.setVisibility(View.GONE);
            }else {
                holder.binding.btnMinus.setVisibility(View.VISIBLE);
                holder.binding.txtQuantity.setVisibility(View.VISIBLE);
                holder.binding.btnMinus.setImageResource(R.drawable.baseline_remove_24);
            }
        }
    }
    private String getPriceFormatted(double unitprice){
        return "S/."+String.format(Locale.getDefault(), "%.2f", unitprice);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemMenuItemBinding binding;
        public ViewHolder(ItemMenuItemBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
