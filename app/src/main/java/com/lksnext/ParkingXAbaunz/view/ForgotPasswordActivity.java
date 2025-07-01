package com.lksnext.ParkingXAbaunz.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingXAbaunz.databinding.ActivityForgotPasswordBinding;
import com.lksnext.ParkingXAbaunz.viewmodel.ForgotPasswordViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel forgotPasswordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        forgotPasswordViewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        forgotPasswordViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                binding.progressBarReset.setVisibility(View.GONE);
                binding.buttonSendResetEmail.setEnabled(true);
            }
        });

        forgotPasswordViewModel.getResetEmailSent().observe(this, sent -> {
            if (sent != null) {
                if (sent) {
                    Toast.makeText(this, "Email de restablecimiento enviado. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
                    finish();
                }
                binding.progressBarReset.setVisibility(View.GONE);
                binding.buttonSendResetEmail.setEnabled(true);
            }
        });

        binding.buttonSendResetEmail.setOnClickListener(v -> {
            String email = binding.editTextEmailReset.getText().toString();
            binding.progressBarReset.setVisibility(View.VISIBLE);
            binding.buttonSendResetEmail.setEnabled(false);
            forgotPasswordViewModel.sendResetPasswordEmail(email);
        });
    }
}
