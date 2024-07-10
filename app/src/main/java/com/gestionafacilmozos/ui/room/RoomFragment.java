package com.gestionafacilmozos.ui.room;

import static androidx.core.app.ActivityCompat.recreate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.SplashActivity;
import com.gestionafacilmozos.adapters.TableAdapter;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentRoomBinding;
import com.gestionafacilmozos.databinding.LayoutRoomLoadingBinding;
import com.gestionafacilmozos.interfaces.OnBackPressedListener;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.repositories.TableRepository;
import com.gestionafacilmozos.ui.room.RoomViewModel;
import com.gestionafacilmozos.utilities.LoadingDialogFragment;

import java.util.List;

public class RoomFragment extends Fragment implements OnBackPressedListener {
    private TableRepository tableRepository;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int limit = 10;
    private int skip = 0;
    private String sort = "ASC";
    private RoomViewModel roomViewModel;
    private TableAdapter tableAdapter;
    private FragmentRoomBinding binding;
    private boolean doubleBackToExitPressedOnce = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;
    private Toast currentToast;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recycler.setLayoutManager(layoutManager);
        tableAdapter = new TableAdapter(); // Initialize with an empty adapter
        binding.recycler.setAdapter(tableAdapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setNestedScrollingEnabled(true); // Enable nested scrolling if necessary
        setupScrollListener(layoutManager);
        observeViewModel();
        if (roomViewModel.getTables().getValue() == null) {
            loadTables();
        } else {
            tableAdapter.setTables(roomViewModel.getTables().getValue());
        }
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
                        loadTables();
                    }
                }
            }
        });
    }
    private void loadTables() {
        isLoading = true;
        tableRepository = new TableRepository(getContext());
        tableRepository.get(MainActivity.getToken(), limit, skip, sort, new ResultCallback.ListTableData() {
            @Override
            public void onSuccess(List<Table> data) {
                if (data.isEmpty()) {
                    isLastPage = true;
                } else {
                    tableAdapter.addTables(data);
                    roomViewModel.setTables(tableAdapter.getCurrentTables());
                    skip++;
                }
                isLoading = false;
            }
            @Override
            public void onError(ErrorResponse errorMessage) {
                isLoading = false;
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.loadErrorMessageAlert(errorMessage);
                }
            }
        });
    }
    private void observeViewModel() {
        roomViewModel.getTables().observe(getViewLifecycleOwner(), tables -> {
            if (tables != null) {
                tableAdapter.setTables(tables);
            }
        });
    }
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Salir de la aplicacion")
                .setMessage("¿Esta seguro que desea salir de la aplicacion?")
                .setPositiveButton("Si", (dialog, which) -> getActivity().finish())
                .setNegativeButton("No", (dialog, which) -> doubleBackToExitPressedOnce = false)
                .show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if (mRunnable != null) {
                mHandler.removeCallbacks(mRunnable);
            }
            if (currentToast != null) {
                currentToast.cancel();
            }
            showExitConfirmationDialog();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        // Cancelar cualquier Toast existente
        if (currentToast != null) {
            currentToast.cancel();
        }
        // Mostrar un nuevo Toast
        currentToast = Toast.makeText(getContext(), "Pulse una vez mas para salir", Toast.LENGTH_SHORT);
        currentToast.show();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        };
        mHandler.postDelayed(mRunnable, 2000); // Establece un tiempo de espera de 2 segundos
    }
}
