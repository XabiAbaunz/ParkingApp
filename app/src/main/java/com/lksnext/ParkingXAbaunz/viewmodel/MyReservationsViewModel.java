package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.util.List;

public class MyReservationsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Reserva>> reservations = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<List<Reserva>> getReservations() {
        return reservations;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadReservations() {
        loading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        DataRepository.getInstance().getReservations(new DataRepository.ReservationsCallback() {
            @Override
            public void onSuccess(List<Reserva> reservationList) {
                loading.setValue(false);
                reservations.setValue(reservationList);
            }

            @Override
            public void onFailure() {
                loading.setValue(false);
                errorMessage.setValue("Error al cargar las reservas");
            }
        });
    }

    public void deleteReservation(String reservationId) {
        loading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        DataRepository.getInstance().deleteReservation(reservationId, new Callback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Reserva eliminada exitosamente");
                loadReservations(); // Reload reservations after successful deletion
            }

            @Override
            public void onFailure() {
                loading.setValue(false);
                errorMessage.setValue("Error al eliminar la reserva");
            }
        });
    }

    public void updateReservation(Reserva reservation) {
        loading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        DataRepository.getInstance().updateReservation(reservation, new Callback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Reserva actualizada exitosamente");
                loadReservations(); // Reload reservations after successful update
            }

            @Override
            public void onFailure() {
                loading.setValue(false);
                errorMessage.setValue("Error al actualizar la reserva");
            }
        });
    }

    public void clearMessages() {
        successMessage.setValue(null);
        errorMessage.setValue(null);
    }
}