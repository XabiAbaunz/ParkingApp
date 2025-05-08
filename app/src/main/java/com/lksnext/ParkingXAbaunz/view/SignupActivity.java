package com.lksnext.ParkingXAbaunz.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

        // Asignamos la vista/interfaz signup (layout)
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Asignamos el viewModel de signup
        signupViewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        // Restablecer los datos almacenados en el ViewModel (para mantenerlos en rotación)
        signupViewModel.getEmail().observe(this, email -> {
            if (email != null && !email.equals(binding.editTextEmail.getText().toString())) {
                binding.editTextEmail.setText(email);
            }
        });

        signupViewModel.getPassword().observe(this, password -> {
            if (password != null && !password.equals(binding.editTextPassword.getText().toString())) {
                binding.editTextPassword.setText(password);
            }
        });

        signupViewModel.getConfirmPassword().observe(this, confirmPassword -> {
            if (confirmPassword != null && !confirmPassword.equals(binding.editTextConfirmPassword.getText().toString())) {
                binding.editTextConfirmPassword.setText(confirmPassword);
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
                signupViewModel.setEmail(s.toString());
            }
        });

        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                signupViewModel.setPassword(s.toString());
            }
        });

        binding.editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                signupViewModel.setConfirmPassword(s.toString());
            }
        });

        // Observar errores
        signupViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Acción para el botón de registro
        binding.button.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString();
            signupViewModel.registerUser(email, password, confirmPassword);
        });

        signupViewModel.isRegistered().observe(this, registered -> {
            if (registered != null && registered) {
                // Registro Correcto
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                // Crea el intent para ir al DashboardActivity
                Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);

                // Pasar el email del usuario al Dashboard
                intent.putExtra("USER_EMAIL", binding.editTextEmail.getText().toString());

                startActivity(intent);
                finish(); // Cerramos la activity para que no vuelva al presionar atrás
            }
        });
    }
}