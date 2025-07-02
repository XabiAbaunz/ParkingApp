package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.util.List;

public class NewReservationViewModel extends ViewModel {

    private DataRepository repository;
    private MutableLiveData<List<Coche>> cochesLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;

    public NewReservationViewModel() {
        repository = DataRepository.getInstance();
        cochesLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>(false);
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

    public void saveReservation(String fecha, Coche coche, String tipoPlaza, long horaInicio, long horaFin) {
        if (coche == null) {
            errorLiveData.setValue("Debes seleccionar un coche");
            return;
        }

        if (fecha == null || fecha.isEmpty()) {
            errorLiveData.setValue("Debes seleccionar una fecha");
            return;
        }

        if (tipoPlaza == null || tipoPlaza.isEmpty()) {
            errorLiveData.setValue("Debes seleccionar un tipo de plaza");
            return;
        }

        if (horaInicio >= horaFin) {
            errorLiveData.setValue("La hora de fin debe ser posterior a la hora de inicio");
            return;
        }

        isLoadingLiveData.setValue(true);

        Plaza plaza = new Plaza(1, tipoPlaza);
        Hora hora = new Hora(horaInicio, horaFin);
        String reservaId = "temp-" + System.currentTimeMillis();
        Reserva reserva = new Reserva(fecha, coche, reservaId, plaza, hora);

        repository.saveReservation(reserva, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Reserva guardada con éxito");
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }
}