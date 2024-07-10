package com.gestionafacilmozos.ui.room;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.adapters.ComandaDetailsAdapter;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.requests.ComandaRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentOrderTicketBinding;
import com.gestionafacilmozos.databinding.LayoutReviewToPayBinding;
import com.gestionafacilmozos.databinding.LayoutOrderTicketLoadingBinding;
import com.gestionafacilmozos.interfaces.OnBackPressedListener;
import com.gestionafacilmozos.repositories.OrderRepository;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.utilities.LoadingDialogFragment;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

public class OrderTicketFragment extends Fragment implements OnBackPressedListener {
    private Order comanda;
    private Table tableSelected;
    private OrderRepository orderRepository;
    private FragmentOrderTicketBinding binding;
    private LayoutOrderTicketLoadingBinding layoutOrderTicketLoadingBinding;
    private LayoutReviewToPayBinding layoutReviewToPayBinding;
    private TextView subTotalTextView;
    private TextView totalTipTextView;
    private TextView totalTextView;
    public OrderTicketFragment(){
        this.comanda=new Order();
        this.orderRepository=new OrderRepository(getContext());
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String oTable = getArguments().getString("oTable");
            this.tableSelected = new Gson().fromJson(oTable, Table.class);
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
        subTotalTextView = layoutReviewToPayBinding.subTotal;
        totalTipTextView = layoutReviewToPayBinding.totalTipo;
        totalTextView = layoutReviewToPayBinding.total;
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_menu_order_ticket,menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_add) {
                    Bundle bundle=new Bundle();
                    bundle.putString("table",new Gson().toJson(tableSelected));
                    bundle.putString("comanda",new Gson().toJson(comanda));
                    Navigation.findNavController(view).navigate(R.id.action_navigation_order_ticket_to_menuItemsFragment,bundle);
                    return true;
                }else if(menuItem.getItemId() == android.R.id.home){
                    if(comanda.getDetails().size()==0){
                        deleteOrder();
                    }else{
                        Navigation.findNavController(view).navigate(R.id.action_navigation_order_ticket_to_navigation_room);
                    }

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
        orderRepository=new OrderRepository(getContext());
        if(tableSelected.getOrderIdAssocied()==null){
            createOrder();
        }else{
            getOrder();
        }
    }
    @Override
    public void onBackPressed() {
        if(comanda.getDetails().size()==0){
            deleteOrder();
        }else{
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_order_ticket_to_navigation_room);
        }
    }
    private void getOrder() {
        orderRepository.getOrderInfo(MainActivity.getToken(), tableSelected.getOrderIdAssocied(), new ResultCallback.OrderInfo() {
            @Override
            public void onSuccess(Order order) {
                comanda=order;
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.stopShimmer();
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.setVisibility(View.GONE);
                setupRecyclerView(order.getDetails());
                calculateTotal();
            }
            @Override
            public void onError(ErrorResponse errorMessage) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.loadErrorMessageAlert(errorMessage);
                }
            }
        });
    }

    private void createOrder() {
        orderRepository.createOrder(MainActivity.getToken(), new ComandaRequest(tableSelected.getId()), new ResultCallback.OrderCreate() {
            @Override
            public void onSuccess(Order order) {
                comanda=order;
                tableSelected.setOrderIdAssocied(order.getId());
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.stopShimmer();
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.setVisibility(View.GONE);
                setupRecyclerView(order.getDetails());
                calculateTotal();
            }
            @Override
            public void onError(ErrorResponse errorResponse) {

            }
        });
    }

    private void deleteOrder() {
        LoadingDialogFragment loading = new LoadingDialogFragment("Eliminando orden...");
        loading.show(((MainActivity) getContext()).getSupportFragmentManager(), "loading");
        orderRepository.deleteOrder(MainActivity.getToken(), comanda.getId(), new ResultCallback.Result() {
            @Override
            public void onSuccess(boolean success, String message) {
                loading.dismiss();
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_order_ticket_to_navigation_room);
            }
            @Override
            public void onError(ErrorResponse errorResponse) {

            }
        });
    }

    private void setupRecyclerView(List<OrderDetail> orderDetailList) {
        if (orderDetailList.size() > 0) {
            binding.txtTotalRegister.setVisibility(View.GONE);
            layoutReviewToPayBinding.content.setVisibility(View.VISIBLE);
        } else {
            binding.txtTotalRegister.setVisibility(View.VISIBLE);
            layoutReviewToPayBinding.content.setVisibility(View.GONE);
        }
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        binding.recycler.setLayoutManager(layoutManager);
        ComandaDetailsAdapter adapter = new ComandaDetailsAdapter(comanda, getContext(),this::calculateTotal);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setNestedScrollingEnabled(true);
    }
    private void calculateTotal() {
        double subTotal = 0;
        double totalTip = 0;
        double total = 0;
        double tipPercentage = 0;

        for (OrderDetail detail : comanda.getDetails()) {
            subTotal += detail.getUnitPrice() * detail.getQuantity();
        }

        totalTip = subTotal * tipPercentage;
        total = subTotal + totalTip;

        // Formatear los valores a dos decimales
        String subTotalFormatted = String.format(Locale.getDefault(), "S/%.2f", subTotal);
        String totalTipFormatted = String.format(Locale.getDefault(), "S/%.2f", totalTip);
        String totalFormatted = String.format(Locale.getDefault(), "S/%.2f", total);

        // Actualizar los TextViews
        subTotalTextView.setText(subTotalFormatted);
        totalTipTextView.setText(totalTipFormatted);
        totalTextView.setText(totalFormatted);

        // Mostrar el layout si tiene detalles
        if (total!=0) {
            layoutReviewToPayBinding.content.setVisibility(View.VISIBLE);
            binding.txtTotalRegister.setVisibility(View.GONE);
        }else{
            layoutReviewToPayBinding.content.setVisibility(View.GONE);
            binding.txtTotalRegister.setVisibility(View.VISIBLE);
        }
    }
}