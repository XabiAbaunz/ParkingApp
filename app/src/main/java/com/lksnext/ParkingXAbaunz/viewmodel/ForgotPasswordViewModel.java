package com.lksnext.ParkingXAbaunz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.ParkingXAbaunz.data.DataRepository;
import com.lksnext.ParkingXAbaunz.domain.Callback;

import java.util.regex.Pattern;

public class ForgotPasswordViewModel extends ViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> resetEmailSent = new MutableLiveData<>(null);

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getResetEmailSent() {
        return resetEmailSent;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void sendResetPasswordEmail(String emailAddress) {
        error.setValue(null);
        resetEmailSent.setValue(null);

        if (emailAddress == null || emailAddress.isEmpty()) {
            error.setValue("Por favor, introduce tu correo electrónico.");
            return;
        }

        if (!isValidEmail(emailAddress)) {
            error.setValue("El formato del email no es válido.");
            return;
        }

        DataRepository.getInstance().sendPasswordResetEmail(emailAddress, new Callback() {
            @Override
            public void onSuccess() {
                resetEmailSent.setValue(true);
                error.setValue(null);
            }

            @Override
            public void onFailure(String errorMessage) {
                resetEmailSent.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Simple regex for email validation
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
    }
}