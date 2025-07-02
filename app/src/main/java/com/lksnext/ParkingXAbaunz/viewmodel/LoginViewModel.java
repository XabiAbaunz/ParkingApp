package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> logged = new MutableLiveData<>(null);
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<Boolean> isLogged() {
        return logged;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public void loginUser(String email, String password) {
        clearError();
        clearLogged();

        setEmail(email);
        setPassword(password);

        if (email.isEmpty() || password.isEmpty()) {
            error.setValue("El email y la contraseña son obligatorios");
            logged.setValue(Boolean.FALSE);
            return;
        }

        DataRepository.getInstance().login(email, password, new Callback() {
            @Override
            public void onSuccess() {
                error.setValue(null);
                logged.setValue(Boolean.TRUE);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.setValue(errorMessage);
                logged.setValue(Boolean.FALSE);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearLogged() {
        logged.setValue(null);
    }
}
