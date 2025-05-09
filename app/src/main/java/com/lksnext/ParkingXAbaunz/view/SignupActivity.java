package com.lksnext.ParkingXAbaunz.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingXAbaunz.databinding.ActivitySignupBinding;
import com.lksnext.ParkingXAbaunz.viewmodel.SignupViewModel;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private SignupViewModel signupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        signupViewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        signupViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        binding.button.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString();
            signupViewModel.registerUser(email, password, confirmPassword);
        });

        signupViewModel.isRegistered().observe(this, registered -> {
            if (registered != null && registered) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                intent.putExtra("USER_EMAIL", binding.editTextEmail.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }
}