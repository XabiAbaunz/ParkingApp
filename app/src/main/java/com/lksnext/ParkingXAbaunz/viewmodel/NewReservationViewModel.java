package com.lksnext.ParkingXAbaunz.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.notifications.ReservationNotificationManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewReservationViewModel extends AndroidViewModel {

    private DataRepository repository;
    private ReservationNotificationManager notificationManager;
    private MutableLiveData<List<Coche>> cochesLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;

    public NewReservationViewModel(Application application) {
        super(application);
        repository = DataRepository.getInstance();
        notificationManager = new ReservationNotificationManager(application);
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

        if (!isValidDate(fecha)) {
            errorLiveData.setValue("Solo puedes hacer reservas desde hoy hasta 7 días naturales");
            return;
        }

        long durationSeconds = horaFin - horaInicio;
        long durationHours = durationSeconds / 3600;
        if (durationHours > 8) {
            errorLiveData.setValue("La reserva no puede exceder las 8 horas");
            return;
        }

        isLoadingLiveData.setValue(true);

        repository.checkReservationConflict(fecha, horaInicio, horaFin, new CallbackWithData<Boolean>() {
            @Override
            public void onSuccess(Boolean hasConflict) {
                if (hasConflict) {
                    isLoadingLiveData.setValue(false);
                    errorLiveData.setValue("Ya tienes una reserva que coincide con este horario");
                } else {
                    saveReservationInternal(fecha, coche, tipoPlaza, horaInicio, horaFin);
                }
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue("Error al verificar conflictos: " + error);
            }
        });
    }

    private void saveReservationInternal(String fecha, Coche coche, String tipoPlaza, long horaInicio, long horaFin) {
        Plaza plaza = new Plaza(1, tipoPlaza);
        Hora hora = new Hora(horaInicio, horaFin);
        String reservaId = "temp-" + System.currentTimeMillis();
        Reserva reserva = new Reserva(fecha, coche, reservaId, plaza, hora);

        repository.saveReservation(reserva, new Callback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                successLiveData.setValue("Reserva guardada con éxito");

                // Verificar que notificationManager no sea null antes de usarlo
                if (notificationManager != null) {
                    notificationManager.scheduleNotifications(reserva);
                }
            }

            @Override
            public void onFailure(String error) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    private boolean isValidDate(String fechaStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fecha = formatter.parse(fechaStr);

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
}