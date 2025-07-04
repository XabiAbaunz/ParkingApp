package com.lksnext.ParkingXAbaunz.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.notifications.ReservationNotificationManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReservationsViewModel extends AndroidViewModel {

    private DataRepository repository;
    private ReservationNotificationManager notificationManager;
    private MutableLiveData<List<Reserva>> futureReservationsLiveData;
    private MutableLiveData<List<Reserva>> pastReservationsLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MyReservationsViewModel(Application application) {
        super(application);
        repository = DataRepository.getInstance();
        notificationManager = new ReservationNotificationManager(application);
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
                notificationManager.cancelNotifications(reserva.getId());
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
        if (!isValidReservation(reserva)) {
            return;
        }

        isLoadingLiveData.setValue(true);

        repository.checkReservationConflictForUpdate(
                reserva.getId(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                new CallbackWithData<Boolean>() {
                    @Override
                    public void onSuccess(Boolean hasConflict) {
                        if (hasConflict) {
                            isLoadingLiveData.setValue(false);
                            errorLiveData.setValue("Ya tienes otra reserva que coincide con este horario");
                        } else {
                            updateReservationInternal(reserva);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        isLoadingLiveData.setValue(false);
                        errorLiveData.setValue("Error al verificar conflictos: " + error);
                    }
                }
        );
    }

    private void updateReservationInternal(Reserva reserva) {
        repository.updateReservation(reserva, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Reserva actualizada correctamente");
                notificationManager.updateNotifications(reserva);
                loadReservations();
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    private boolean isValidReservation(Reserva reserva) {
        if (reserva.getCoche() == null) {
            errorLiveData.setValue("Debes seleccionar un coche");
            return false;
        }

        if (reserva.getFecha() == null || reserva.getFecha().isEmpty()) {
            errorLiveData.setValue("Debes seleccionar una fecha");
            return false;
        }

        if (reserva.getHoraInicio() >= reserva.getHoraFin()) {
            errorLiveData.setValue("La hora de fin debe ser posterior a la hora de inicio");
            return false;
        }

        if (!isValidDate(reserva.getFecha())) {
            errorLiveData.setValue("Solo puedes hacer reservas desde hoy hasta 7 días naturales");
            return false;
        }

        long durationSeconds = reserva.getHoraFin() - reserva.getHoraInicio();
        long durationHours = durationSeconds / 3600;
        if (durationHours > 8) {
            errorLiveData.setValue("La reserva no puede exceder las 8 horas");
            return false;
        }

        return true;
    }

    private boolean isValidDate(String fechaStr) {
        try {
            Date fecha = dateFormat.parse(fechaStr);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.DAY_OF_MONTH, 7);
            maxDate.set(Calendar.HOUR_OF_DAY, 23);
            maxDate.set(Calendar.MINUTE, 59);
            maxDate.set(Calendar.SECOND, 59);
            maxDate.set(Calendar.MILLISECOND, 999);

            return fecha != null &&
                    !fecha.before(today.getTime()) &&
                    !fecha.after(maxDate.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    public void getCoches(CallbackWithData<List<Coche>> callback) {
        repository.getCoches(callback);
    }

    public void clearMessages() {
        errorLiveData.setValue(null);
        successLiveData.setValue(null);
    }
}