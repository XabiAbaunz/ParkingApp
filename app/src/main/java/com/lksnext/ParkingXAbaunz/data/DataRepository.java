package com.lksnext.ParkingXAbaunz.data;

import com.lksnext.ParkingXAbaunz.domain.Callback;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.util.List;

public class DataRepository {

    private static DataRepository instance;
    private DataRepository(){
    }

    public static synchronized DataRepository getInstance(){
        if (instance==null){
            instance = new DataRepository();
        }
        return instance;
    }

    public void login(String email, String pass, Callback callback){
        try {
            callback.onSuccess();
        } catch (Exception e){
            callback.onFailure();
        }
    }

    public void register(String email, String password, Callback callback) {
        try {
            callback.onSuccess();
        } catch (Exception e){
            callback.onFailure();
        }
    }

    public interface ReservationsCallback {
        void onSuccess(List<Reserva> reservations);
        void onFailure();
    }

    public void getReservations(ReservationsCallback callback) {
        try {
            // Simulate successful data retrieval with empty list for now
            callback.onSuccess(new java.util.ArrayList<>());
        } catch (Exception e) {
            callback.onFailure();
        }
    }

    public void deleteReservation(String reservationId, Callback callback) {
        try {
            // Simulate successful deletion
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFailure();
        }
    }

    public void updateReservation(Reserva reservation, Callback callback) {
        try {
            // Simulate successful update
            callback.onSuccess();
        } catch (Exception e) {
            callback.onFailure();
        }
    }
}

