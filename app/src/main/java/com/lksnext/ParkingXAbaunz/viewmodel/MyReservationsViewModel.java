package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReservationsViewModel extends ViewModel {

    private DataRepository repository;
    private MutableLiveData<List<Reserva>> futureReservationsLiveData;
    private MutableLiveData<List<Reserva>> pastReservationsLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MyReservationsViewModel() {
        repository = DataRepository.getInstance();
        futureReservationsLiveData = new MutableLiveData<>();
        pastReservationsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Reserva>> getFutureReservationsLiveData() {
        return futureReservationsLiveData;
    }

    public LiveData<List<Reserva>> getPastReservationsLiveData() {
        return pastReservationsLiveData;
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

    public void loadReservations() {
        isLoadingLiveData.setValue(true);

        repository.getReservations(new CallbackWithData<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                processReservations(reservas);
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
                isLoadingLiveData.setValue(false);
            }
        });
    }

    public void deleteReservation(Reserva reserva) {
        isLoadingLiveData.setValue(true);

        repository.deleteReservation(reserva.getId(), new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Reserva eliminada correctamente");
                loadReservations();
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    private void processReservations(List<Reserva> reservas) {
        List<Reserva> futureReservations = new ArrayList<>();
        List<Reserva> pastReservations = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Date todayDate = today.getTime();

        for (Reserva reserva : reservas) {
            try {
                Date reservaDate = dateFormat.parse(reserva.getFecha());
                if (reservaDate != null && (reservaDate.after(todayDate) || reservaDate.equals(todayDate))) {
                    futureReservations.add(reserva);
                } else {
                    pastReservations.add(reserva);
                }
            } catch (ParseException e) {
                futureReservations.add(reserva);
            }
        }

        futureReservations.sort((r1, r2) -> {
            try {
                Date date1 = dateFormat.parse(r1.getFecha());
                Date date2 = dateFormat.parse(r2.getFecha());

                int dateComparison = date1.compareTo(date2);
                if (dateComparison != 0) {
                    return dateComparison;
                }

                return Long.compare(r1.getHoraInicio(), r2.getHoraInicio());
            } catch (ParseException e) {
                return 0;
            }
        });

        pastReservations.sort((r1, r2) -> {
            try {
                Date date1 = dateFormat.parse(r1.getFecha());
                Date date2 = dateFormat.parse(r2.getFecha());

                int dateComparison = date2.compareTo(date1);
                if (dateComparison != 0) {
                    return dateComparison;
                }

                return Long.compare(r2.getHoraInicio(), r1.getHoraInicio());
            } catch (ParseException e) {
                return 0;
            }
        });

        futureReservationsLiveData.setValue(futureReservations);
        pastReservationsLiveData.setValue(pastReservations);
    }

    public void updateReservation(Reserva reserva) {
        isLoadingLiveData.setValue(true);

        repository.updateReservation(reserva, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Reserva actualizada correctamente");
                loadReservations();
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void getCoches(CallbackWithData<List<Coche>> callback) {
        repository.getCoches(callback);
    }
}