package com.lksnext.ParkingXAbaunz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
            if (logged != null) {
                if (logged) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_EMAIL", binding.editTextEmail.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.button.setEnabled(true);
            }
        });

        loginViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.button.setEnabled(true);
            }
        });


        binding.button.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.button.setEnabled(false);

            loginViewModel.loginUser(email, password);
        });

        binding.textView2.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}
