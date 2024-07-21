package com.gestionafacilmozos.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.ItemOrderDetailBinding;
import com.gestionafacilmozos.repositories.DispatchAreaRepository;
import com.gestionafacilmozos.repositories.OrderDetailRepository;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.utilities.LoadingDialogFragment;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ComandaDetailsAdapter extends RecyclerView.Adapter<ComandaDetailsAdapter.ViewHolder>{
    private Order comanda;
    private OrderDetailRepository orderDetailRepository;
    private DispatchAreaRepository dispatchAreaRepository;
    private Context context;
    private Runnable onQuantityChangedCallback;
    public ComandaDetailsAdapter(Order order, Context context, Runnable onQuantityChangedCallback) {
        this.comanda = order;
        this.context = context;
        this.orderDetailRepository=new OrderDetailRepository();
        this.dispatchAreaRepository=new DispatchAreaRepository(context);
        this.onQuantityChangedCallback = onQuantityChangedCallback;
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
                OrderDetail item=comanda.getDetails().get(position);
                int quantity=item.getQuantity()+1;
                updateOrderDetailQuantity(holder,item,quantity,false);
            }
        });
        holder.binding.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity=item.getQuantity()-1;
                if(item.getQuantityReceivedForDispatchArea()==0){
                    if(quantity>0){
                        updateOrderDetailQuantity(holder,item,quantity,true);
                    }else{
                        showExitConfirmationDialog(holder,item);
                    }
                }else{
                    if((quantity+1)>item.getQuantityReceivedForDispatchArea()){
                        OrderDetail newItem=comanda.getDetails().get(position);
                        updateOrderDetailQuantity(holder,newItem,quantity,true);
                    }else{
                        alertMessage(item);
                    }
                }
            }
        });
    }
    private void alertMessage(OrderDetail item) {
        new AlertDialog.Builder(context)
                .setTitle("No puedes eliminar el registro:")
                .setMessage("El registro "+item.getMenuItem().getDenomination()+" se encuentra en area de despacho")
                .setPositiveButton("Aceptar", null)
                .show();
    }
    private void showExitConfirmationDialog(ViewHolder holder, OrderDetail item) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar acción:")
                .setMessage("¿Esta seguro que desea eliminar el registro "+item.getMenuItem().getDenomination()+"?")
                .setPositiveButton("Si", (dialog, which) -> removeMenuItem(holder,item))
                .setNegativeButton("No", null)
                .show();
    }
    private void removeMenuItem(ViewHolder holder, OrderDetail item) {
        int index=getOrderDetailIndex(item.getMenuItem()).orElse(-1);
        OrderDetail orderDetailToRemove=comanda.getDetails().get(index);
        LoadingDialogFragment loading = new LoadingDialogFragment("Eliminando registro...");
        loading.show(((MainActivity) context).getSupportFragmentManager(), "loading");

        orderDetailRepository.deleteOrderDetail(MainActivity.getToken(), orderDetailToRemove.getId(), new ResultCallback.Result() {
            @Override
            public void onSuccess(boolean success, String message) {
                loading.dismiss();
                comanda.getDetails().remove(index);
                notifyDataSetChanged();
                onQuantityChangedCallback.run();
            }
            @Override
            public void onError(ErrorResponse errorResponse) {

            }
        });
    }
    private void updateOrderDetailQuantity(ViewHolder holder, OrderDetail item, int quantity,boolean btnMinus) {
        LoadingDialogFragment loading = new LoadingDialogFragment("Actualizando registro...");
        loading.show(((MainActivity) context).getSupportFragmentManager(), "loading");
        orderDetailRepository.updateQuantityOfOrderDetail(MainActivity.getToken(), item.getId(), quantity, new ResultCallback.Result() {
            @Override
            public void onSuccess(boolean success, String message) {
                if(btnMinus){
                    dispatchAreaRepository.deleteDispatchAreaByOrderDetailIdAndStateId(MainActivity.getToken(), item.getId(), 1L, 1, new ResultCallback.DispatchAreaDelete() {
                        @Override
                        public void onSuccess(Long num_items_affected) {
                            loading.dismiss();
                            int index=getOrderDetailIndex(item.getMenuItem()).orElse(-1);
                            comanda.getDetails().get(index).setQuantity(quantity);
                            notifyDataSetChanged();
                            updateControls(holder,item);
                            onQuantityChangedCallback.run();
                        }
                        @Override
                        public void onError(ErrorResponse errorResponse) {

                        }
                    });
                }else{
                    loading.dismiss();
                    int index=getOrderDetailIndex(item.getMenuItem()).orElse(-1);
                    comanda.getDetails().get(index).setQuantity(quantity);
                    notifyDataSetChanged();
                    updateControls(holder,item);
                    onQuantityChangedCallback.run();
                }

            }
            @Override
            public void onError(ErrorResponse errorResponse) {

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
    private Optional<Integer> getOrderDetailIndex(MenuItem item) {
        return Optional.of(comanda.getDetails().stream()
                .map(OrderDetail::getMenuItem)
                .map(MenuItem::getId)
                .collect(Collectors.toList())
                .indexOf(item.getId()));
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ItemOrderDetailBinding binding;
        public ViewHolder(ItemOrderDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
