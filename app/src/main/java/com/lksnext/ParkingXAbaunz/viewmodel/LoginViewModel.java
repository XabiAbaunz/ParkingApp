package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;

public class LoginViewModel extends ViewModel {

    // LiveData para mantener el estado de la sesión y los datos de usuario
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
        // Guardar los valores en el ViewModel para persistir en rotaciones
        setEmail(email);
        setPassword(password);

        // Validación básica
        if (email.isEmpty() || password.isEmpty()) {
            error.setValue("El email y la contraseña son obligatorios");
            return;
        }

        // Usamos el DataRepository como en la implementación original
        DataRepository.getInstance().login(email, password, new Callback() {
            @Override
            public void onSuccess() {
                error.setValue(null);
                logged.setValue(Boolean.TRUE);
            }

            @Override
            public void onFailure() {
                error.setValue("Error de inicio de sesión");
                logged.setValue(Boolean.FALSE);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }
}