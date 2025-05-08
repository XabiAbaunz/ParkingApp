package com.lksnext.ParkingXAbaunz.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;

public class SignupViewModel extends ViewModel {

    // LiveData para mantener el estado del registro y los datos de usuario
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
        // Guardar los valores en el ViewModel para persistir en rotaciones
        setEmail(email);
        setPassword(password);
        setConfirmPassword(confirmPassword);

        // Validación de campos
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            error.setValue("Todos los campos son obligatorios");
            return;
        }

        // Validación de formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error.setValue("El formato del email no es válido");
            return;
        }

        // Validación de coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            error.setValue("Las contraseñas no coinciden");
            return;
        }

        // Validación de longitud mínima de contraseña
        if (password.length() < 6) {
            error.setValue("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Usamos el DataRepository para el registro
        DataRepository.getInstance().register(email, password, new Callback() {
            @Override
            public void onSuccess() {
                error.setValue(null);
                registered.setValue(Boolean.TRUE);
            }

            @Override
            public void onFailure() {
                error.setValue("Error en el registro. Inténtalo de nuevo.");
                registered.setValue(Boolean.FALSE);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }
}