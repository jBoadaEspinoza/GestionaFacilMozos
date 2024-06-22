package com.gestionafacilmozos.ui.room;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gestionafacilmozos.R;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.databinding.FragmentOrdenTicketBinding;
import com.google.gson.Gson;

public class OrdenTicketFragment extends Fragment {
    private Table tableSelected;
    private FragmentOrdenTicketBinding binding;
    public OrdenTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu in the fragment
        if (getArguments() != null) {
            String oTable = getArguments().getString("oTable");
            this.tableSelected=new Gson().fromJson(oTable,Table.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentOrdenTicketBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (tableSelected != null && getActivity() != null) {
            AppCompatActivity app=((AppCompatActivity) getActivity());
            app.getSupportActionBar().setTitle(tableSelected.getDenomination().toUpperCase().toString()+"(Comanda)");
            app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),1);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(binding.getRoot()).navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}