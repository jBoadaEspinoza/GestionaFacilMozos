package com.gestionafacilmozos.ui.room;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gestionafacilmozos.MainActivity;
import com.gestionafacilmozos.adapters.TableAdapter;
import com.gestionafacilmozos.api.models.Table;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.FragmentRoomBinding;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.repositories.TableRepository;
import com.gestionafacilmozos.ui.room.RoomViewModel;

import java.util.List;

public class RoomFragment extends Fragment {
    private TableRepository tableRepository;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int limit = 10;
    private int skip = 0;
    private String sort = "ASC";
    private RoomViewModel roomViewModel;
    private TableAdapter tableAdapter;
    private FragmentRoomBinding binding;

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

        // Check if data is already loaded
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
        tableRepository = new TableRepository();
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
                Toast.makeText(getContext(), errorMessage.getMessage(), Toast.LENGTH_LONG).show();
                isLoading = false;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
