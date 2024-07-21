package com.gestionafacilmozos.ui.room;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.adapters.ComandaDetailsAdapter;
import com.gestionafacilmozos.api.models.Order;
import com.gestionafacilmozos.api.models.OrderDetail;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.requests.ComandaBitacoraRequest;
import com.gestionafacilmozos.api.requests.ComandaRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentOrderTicketBinding;
import com.gestionafacilmozos.databinding.LayoutReviewToPayBinding;
import com.gestionafacilmozos.databinding.LayoutOrderTicketLoadingBinding;
import com.gestionafacilmozos.interfaces.OnBackPressedListener;
import com.gestionafacilmozos.repositories.DispatchAreaRepository;
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
    private DispatchAreaRepository dispatchAreaRepository;
    private FragmentOrderTicketBinding binding;
    private LayoutOrderTicketLoadingBinding layoutOrderTicketLoadingBinding;
    private LayoutReviewToPayBinding layoutReviewToPayBinding;
    private TextView subTotalTextView;
    private TextView totalItemsSendingTextView;
    private TextView totalTextView;
    private MenuItem sendOrderMenuItem;
    private int totalItems;
    private int numItemsRegistered;

    public OrderTicketFragment() {
        this.comanda = new Order();
        this.orderRepository = new OrderRepository(getContext());
        this.dispatchAreaRepository = new DispatchAreaRepository(getContext());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderTicketBinding.inflate(inflater, container, false);
        layoutReviewToPayBinding = LayoutReviewToPayBinding.bind(binding.review.getRoot());
        layoutOrderTicketLoadingBinding = LayoutOrderTicketLoadingBinding.bind(binding.shimmerLoading.getRoot());
        layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.startShimmer();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUIComponents(view);
        setupMenu(view);
        fetchOrderData();
    }

    private void setupUIComponents(@NonNull View view) {
        subTotalTextView = layoutReviewToPayBinding.subTotal;
        totalItemsSendingTextView = layoutReviewToPayBinding.totalItemsSending;
        totalTextView = layoutReviewToPayBinding.total;

        if (tableSelected != null && getActivity() != null) {
            AppCompatActivity app = (AppCompatActivity) getActivity();
            app.getSupportActionBar().setTitle(tableSelected.getDenomination().toUpperCase() + "(Comanda)");
            app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupMenu(@NonNull View view) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_menu_order_ticket, menu);
                sendOrderMenuItem = menu.findItem(R.id.action_send_order);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case android.R.id.home:
                        handleBackNavigation();
                        return true;
                    default:
                        if (menuItem.getItemId() == R.id.action_add) {
                            navigateToAddMenuItem(view);
                        }
                        if (menuItem.getItemId() == R.id.action_send_order) {
                            showAlertChooseOption();
                        }
                        return true;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void fetchOrderData() {
        orderRepository = new OrderRepository(getContext());
        if (tableSelected.getOrderIdAssocied() == null) {
            createOrder();
        } else {
            getOrder();
        }
    }

    private void navigateToAddMenuItem(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("table", new Gson().toJson(tableSelected));
        bundle.putString("comanda", new Gson().toJson(comanda));
        Navigation.findNavController(view).navigate(R.id.action_navigation_order_ticket_to_menuItemsFragment, bundle);
    }

    private void showAlertChooseOption() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_choose_option_menu_item, null);
        final RadioGroup radioGroup = alertLayout.findViewById(R.id.radioGroup);
        final RadioButton radioButtonTodos = alertLayout.findViewById(R.id.radioTodos);
        final RadioButton radioButtonPersonalizado = alertLayout.findViewById(R.id.radioPersonalizado);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Seleccionar opción de envio");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        alert.setPositiveButton("Aceptar", (dialog, which) -> handleOptionSelection(radioGroup, radioButtonTodos));
        alert.create().show();
    }

    private void handleOptionSelection(RadioGroup radioGroup, RadioButton radioButtonTodos) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == radioButtonTodos.getId()) {
            sendAllMenuIemToDispatchArea();
        } else {
            // Acción para "Personalizado"
        }
    }

    private void sendAllMenuIemToDispatchArea() {
        LoadingDialogFragment loading = new LoadingDialogFragment("Procesando...");
        loading.show(((MainActivity) getContext()).getSupportFragmentManager(), "loading");

        ComandaBitacoraRequest input = new ComandaBitacoraRequest(tableSelected.getOrderIdAssocied(), 1);
        dispatchAreaRepository.sendAllItemsToDispatchArea(MainActivity.getToken(), input, new ResultCallback.DispatchAreaAdd() {
            @Override
            public void onSuccess(Long num_items_added, Long num_items_registered) {
                loading.dismiss();
                calculateTotal();
                Toast.makeText(getContext(), "Total items agregados " + num_items_added, Toast.LENGTH_LONG).show();
                updateSendOrderMenuItemVisibility();
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                handleErrorResponse(errorResponse);
            }
        });
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if (comanda.getDetails().isEmpty()) {
            deleteOrder();
        } else {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_order_ticket_to_navigation_room);
        }
    }

    private void handleErrorResponse(ErrorResponse errorResponse) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.loadErrorMessageAlert(errorResponse);
        }
    }

    private void getOrder() {
        orderRepository.getOrderInfo(MainActivity.getToken(), tableSelected.getOrderIdAssocied(), new ResultCallback.OrderInfo() {
            @Override
            public void onSuccess(Order order) {
                comanda = order;
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.stopShimmer();
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.setVisibility(View.GONE);
                setupRecyclerView(order.getDetails());
                calculateTotal();
                updateSendOrderMenuItemVisibility();
            }

            @Override
            public void onError(ErrorResponse errorMessage) {
                handleErrorResponse(errorMessage);
            }
        });
    }

    private void createOrder() {
        orderRepository.createOrder(MainActivity.getToken(), new ComandaRequest(tableSelected.getId()), new ResultCallback.OrderCreate() {
            @Override
            public void onSuccess(Order order) {
                comanda = order;
                tableSelected.setOrderIdAssocied(order.getId());
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.stopShimmer();
                layoutOrderTicketLoadingBinding.shimmerContentOrdenTicket.setVisibility(View.GONE);
                setupRecyclerView(order.getDetails());
                calculateTotal();
                updateSendOrderMenuItemVisibility();
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                handleErrorResponse(errorResponse);
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
                handleErrorResponse(errorResponse);
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
        ComandaDetailsAdapter adapter = new ComandaDetailsAdapter(comanda, getContext(), this::calculateTotal);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setNestedScrollingEnabled(true);
    }

    private void calculateTotal() {
        totalItems = 0;
        double total = 0;

        for (OrderDetail detail : comanda.getDetails()) {
            totalItems += detail.getQuantity();
            total += detail.getUnitPrice() * detail.getQuantity();
        }

        String totalFormatted = String.format(Locale.getDefault(), "S/%.2f", total);

        subTotalTextView.setText(String.valueOf(totalItems));
        totalTextView.setText(totalFormatted);

        double finalTotal = total;
        dispatchAreaRepository.get(MainActivity.getToken(), comanda.getId(), 1L, new ResultCallback.DispatchArea() {
            @Override
            public void onSuccess(Long num_items_registered) {
                numItemsRegistered = num_items_registered.intValue();
                totalItemsSendingTextView.setText(String.valueOf(numItemsRegistered));
                updateSendOrderMenuItemVisibility();

                if (finalTotal != 0) {
                    layoutReviewToPayBinding.content.setVisibility(View.VISIBLE);
                    binding.txtTotalRegister.setVisibility(View.GONE);
                } else {
                    layoutReviewToPayBinding.content.setVisibility(View.GONE);
                    binding.txtTotalRegister.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                handleErrorResponse(errorResponse);
            }
        });
    }

    private void updateSendOrderMenuItemVisibility() {
        if (sendOrderMenuItem != null) {
            sendOrderMenuItem.setVisible(totalItems != numItemsRegistered);
        }
    }
}
