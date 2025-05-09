package com.lksnext.ParkingXAbaunz.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingXAbaunz.databinding.ActivityLoginBinding;
import com.lksnext.ParkingXAbaunz.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null && logged) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("USER_EMAIL", binding.editTextEmail.getText().toString());
                startActivity(intent);
                finish();
            }
        });

        binding.button.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            loginViewModel.loginUser(email, password);
        });

        binding.textView2.setOnClickListener(v -> {
            Toast.makeText(this, "Función de recuperación de contraseña no implementada", Toast.LENGTH_SHORT).show();
        });
    }
}