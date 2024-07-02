package com.gestionafacilmozos.ui.room;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.adapters.MenuItemAdapter;
import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentMenuItemsBinding;
import com.gestionafacilmozos.databinding.LayoutMenuItemsLoadingBinding;
import com.gestionafacilmozos.repositories.MenuItemRepository;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MenuItemsFragment extends Fragment {
    private android.view.MenuItem searchItem;
    private GridLayoutManager layoutManager;
    private LayoutMenuItemsLoadingBinding layoutMenuItemsLoadingBinding;
    private MenuItemAdapter adapter;
    private MenuItemRepository menuItemRepository;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int limit=25;
    private int skip=0;
    private String sort="desc";
    private String denomination="";
    private FragmentMenuItemsBinding binding;

    private List<OrderDetail> comanda;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonOrderDetailList = getArguments().getString("orderDetailList");
            Type listType=new TypeToken<List<OrderDetail>>() {}.getType();
            comanda=new Gson().fromJson(jsonOrderDetailList,listType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMenuItemsBinding.inflate(inflater,container,false);
        layoutMenuItemsLoadingBinding=LayoutMenuItemsLoadingBinding.bind(binding.shimmerLoading.getRoot());

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_menu_menu_item,menu);
                searchItem=menu.findItem(R.id.action_search);
                SearchView searchView=(SearchView) searchItem.getActionView();
                searchItem.expandActionView();
                searchView.setIconified(false);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        denomination=query;
                        skip=0;
                        adapter.clearMenuItems();
                        setupScrollListener(layoutManager);
                        loadMenuItems();
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String query) {
                        return false;
                    }
                });
            }
            @Override
            public boolean onMenuItemSelected(@NonNull android.view.MenuItem menuItem) {
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
    private void initialize(){
        layoutManager=new GridLayoutManager(getContext(),1);
        binding.recycler.setLayoutManager(layoutManager);
        Log.d("hola",new Gson().toJson(comanda));
        adapter=new MenuItemAdapter(getContext(),comanda);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setNestedScrollingEnabled(true);
        setupScrollListener(layoutManager);
        loadMenuItems();
    }
    private void loadMenuItems(){
        isLoading = true;
        layoutMenuItemsLoadingBinding.shimmerContentMenuItems.startShimmer();
        layoutMenuItemsLoadingBinding.shimmerContentMenuItems.setVisibility(View.VISIBLE);
        menuItemRepository=new MenuItemRepository();
        menuItemRepository.get(MainActivity.getToken(), limit, skip, sort, denomination, new ResultCallback.ListMenuItemData() {
            @Override
            public void onSuccess(List<MenuItem> data) {
                layoutMenuItemsLoadingBinding.shimmerContentMenuItems.stopShimmer();
                layoutMenuItemsLoadingBinding.shimmerContentMenuItems.setVisibility(View.GONE);
                if(data.isEmpty()){
                    isLastPage=true;
                }else{
                    adapter.addMenuItem(data);
                    skip++;
                }
                isLoading=false;
            }
            @Override
            public void onError(ErrorResponse errorResponse) {
                Toast.makeText(getContext(), errorResponse.getMessage(), Toast.LENGTH_LONG).show();
                isLoading = false;
            }
        });
    }
    private void setupScrollListener(GridLayoutManager layoutManager) {
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= limit) {
                    loadMenuItems();
                }
            }
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        if (searchItem != null) {
            searchItem.collapseActionView();
        }
    }
}