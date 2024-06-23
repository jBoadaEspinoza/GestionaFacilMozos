package com.gestionafacilmozos.ui.room;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.adapters.OrderDetailAdapter;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentOrderTicketBinding;
import com.gestionafacilmozos.databinding.LayoutReviewToPayBinding;
import com.gestionafacilmozos.databinding.LayoutOrderTicketLoadingBinding;
import com.gestionafacilmozos.repositories.OrderRepository;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.google.gson.Gson;
import java.util.List;
public class OrderTicketFragment extends Fragment {
    private Table tableSelected;
    private FragmentOrderTicketBinding binding;
    private LayoutOrderTicketLoadingBinding layoutOrderTicketLoadingBinding;
    private LayoutReviewToPayBinding layoutReviewToPayBinding;
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
        binding=FragmentOrderTicketBinding.inflate(inflater,container,false);
        layoutReviewToPayBinding= LayoutReviewToPayBinding.bind(binding.review.getRoot());
        layoutOrderTicketLoadingBinding= LayoutOrderTicketLoadingBinding.bind(binding.shimmerLoading.getRoot());
        layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.startShimmer();
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_menu_order_ticket,menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_add) {
                    Navigation.findNavController(view).navigate(R.id.action_navigation_order_ticket_to_menuItemsFragment);
                    return true;
                }
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        if (tableSelected != null && getActivity() != null) {
            AppCompatActivity app=((AppCompatActivity) getActivity());
            app.getSupportActionBar().setTitle(tableSelected.getDenomination().toUpperCase().toString()+"(Comanda)");
            app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        OrderRepository orderRepository=new OrderRepository();
        orderRepository.getOrderInfo(MainActivity.getToken(), tableSelected.getOrderIdAssocied(), new ResultCallback.OrderInfo() {
            @Override
            public void onSuccess(Order order) {
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.stopShimmer();
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.setVisibility(View.GONE);
                List<OrderDetail> orderDetailListFromServer=order.getDetails();
                if(orderDetailListFromServer.size()>0){
                    binding.txtTotalRegister.setVisibility(View.GONE);
                    layoutReviewToPayBinding.content.setVisibility(View.VISIBLE);
                }else{
                    binding.txtTotalRegister.setVisibility(View.VISIBLE);
                    layoutReviewToPayBinding.content.setVisibility(View.GONE);
                }
                GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
                binding.recycler.setLayoutManager(layoutManager);
                OrderDetailAdapter adapter=new OrderDetailAdapter(orderDetailListFromServer,getContext());
                binding.recycler.setAdapter(adapter);
                binding.recycler.setHasFixedSize(true);
                binding.recycler.setNestedScrollingEnabled(true);
            }
            @Override
            public void onError(ErrorResponse errorResponse) {}
        });
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