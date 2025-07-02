package com.lksnext.ParkingXAbaunz.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;

public class SignupViewModel extends ViewModel {

    private final MutableLiveData<Boolean> registered = new MutableLiveData<>(null);
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>("");
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<Boolean> isRegistered() {
        return registered;
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

    public LiveData<String> getConfirmPassword() {
        return confirmPassword;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword.setValue(confirmPassword);
    }

    public void registerUser(String email, String password, String confirmPassword) {
        clearError();

        setEmail(email);
        setPassword(password);
        setConfirmPassword(confirmPassword);

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            error.setValue("Todos los campos son obligatorios");
            registered.setValue(Boolean.FALSE);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error.setValue("El formato del email no es válido");
            registered.setValue(Boolean.FALSE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            error.setValue("Las contraseñas no coinciden");
            registered.setValue(Boolean.FALSE);
            return;
        }

        if (password.length() < 6) {
            error.setValue("La contraseña debe tener al menos 6 caracteres");
            registered.setValue(Boolean.FALSE);
            return;
        }

        DataRepository.getInstance().register(email, password, new Callback() {
            @Override
            public void onSuccess() {
                error.setValue(null);
                registered.setValue(Boolean.TRUE);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.setValue(errorMessage);
                registered.setValue(Boolean.FALSE);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
        registered.setValue(null);
    }
}
