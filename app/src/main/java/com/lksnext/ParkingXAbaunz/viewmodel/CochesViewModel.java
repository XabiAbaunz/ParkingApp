package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;

import java.util.List;

public class CochesViewModel extends ViewModel {

    private DataRepository repository;
    private MutableLiveData<List<Coche>> cochesLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;

    public CochesViewModel() {
        repository = DataRepository.getInstance();
        cochesLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
    }

    public CochesViewModel(DataRepository repository) {
        this.repository = repository;
        cochesLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Coche>> getCochesLiveData() {
        return cochesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void loadCoches() {
        isLoadingLiveData.setValue(true);
        repository.getCoches(new CallbackWithData<List<Coche>>() {
            @Override
            public void onSuccess(List<Coche> coches) {
                isLoadingLiveData.setValue(false);
                cochesLiveData.setValue(coches);
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void addCoche(String matricula, String marca, String modelo) {
        if (matricula.trim().isEmpty() || marca.trim().isEmpty() || modelo.trim().isEmpty()) {
            errorLiveData.setValue("Todos los campos son obligatorios");
            return;
        }

        Coche coche = new Coche(matricula.trim(), marca.trim(), modelo.trim());
        isLoadingLiveData.setValue(true);

        repository.addCoche(coche, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Coche añadido correctamente");
                loadCoches();
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteCoche(String matricula) {
        isLoadingLiveData.setValue(true);
        repository.deleteCoche(matricula, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Coche eliminado correctamente");
                loadCoches();
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }
}