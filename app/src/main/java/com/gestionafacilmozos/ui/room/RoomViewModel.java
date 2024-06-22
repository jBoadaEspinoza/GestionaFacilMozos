package com.gestionafacilmozos.ui.room;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gestionafacilmozos.api.models.Table;

import java.util.List;

public class RoomViewModel extends ViewModel {
    private final MutableLiveData<List<Table>> tables = new MutableLiveData<>();

    public LiveData<List<Table>> getTables() {
        return tables;
    }

    public void setTables(List<Table> newTables) {
        tables.setValue(newTables);
    }
}
