package com.lksnext.ParkingXAbaunz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

        //Asignamos la vista/interfaz login (layout)
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Asignamos el viewModel de login
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Restablecer los datos almacenados en el ViewModel (para mantenerlos en rotación)
        loginViewModel.getEmail().observe(this, email -> {
            if (email != null && !email.equals(binding.editTextEmail.getText().toString())) {
                binding.editTextEmail.setText(email);
            }
        });

        loginViewModel.getPassword().observe(this, password -> {
            if (password != null && !password.equals(binding.editTextPassword.getText().toString())) {
                binding.editTextPassword.setText(password);
            }
        });

        // Configurar TextWatchers para actualizar el ViewModel con cada cambio
        binding.editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.setEmail(s.toString());
            }
        });

        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.setPassword(s.toString());
            }
        });

        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null && logged) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);

                intent.putExtra("USER_EMAIL", binding.editTextEmail.getText().toString());

                startActivity(intent);
                // Opcional: finish(); // Si no quieres que el usuario pueda volver atrás
            } else {
                // Login incorrecto - Error ya mostrado a través del Toast
            }
        });

        // Acción para el botón de iniciar sesión
        binding.button.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            loginViewModel.loginUser(email, password);
        });

        // Acción para el texto de "¿Has olvidado tu contraseña?"
        binding.textView2.setOnClickListener(v -> {
            Toast.makeText(this, "Función de recuperación de contraseña no implementada", Toast.LENGTH_SHORT).show();
            // Aquí podrías implementar la navegación a la página de recuperación de contraseña
        });

        //Observamos la variable logged, la cual nos informará cuando el usuario intente hacer login
        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null) {
                if (logged) {
                    //Login Correcto
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    //Login incorrecto - Error ya mostrado a través del Toast
                }
            }
        });
    }
}