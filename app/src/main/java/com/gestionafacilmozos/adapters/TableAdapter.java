package com.gestionafacilmozos.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.databinding.ItemTableBinding;
import com.gestionafacilmozos.databinding.LayoutRoomLoadingBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {
    private List<Table> listTable;
    public TableAdapter() {
        this.listTable = new ArrayList<>();
    }

    @NonNull
    @Override
    public TableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTableBinding binding = ItemTableBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TableAdapter.ViewHolder holder, int position) {
        Table table = listTable.get(position);
        holder.binding.txtDenomination.setText(table.getDenomination());
        if (table.isOccupied()) {
            holder.binding.imgTable.setImageResource(R.drawable.tb_occupied);
        } else {
            holder.binding.imgTable.setImageResource(R.drawable.tb_unoccupied);
        }
        holder.binding.getRoot().setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("oTable", new Gson().toJson(table));
            Navigation.findNavController(view).navigate(R.id.action_navigation_room_to_ordenTicketFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return listTable.size();
    }

    public void setTables(List<Table> tables) {
        this.listTable = tables;
        notifyDataSetChanged();
    }

    public void addTables(List<Table> newTables) {
        int startPosition = listTable.size();
        listTable.addAll(newTables);
        notifyItemRangeInserted(startPosition, newTables.size());
    }

    public List<Table> getCurrentTables() {
        return listTable;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTableBinding binding;

        public ViewHolder(@NonNull ItemTableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
