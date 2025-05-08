package com.lksnext.ParkingXAbaunz.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.ActivityDashboardBinding;
import com.lksnext.ParkingXAbaunz.view.fragments.MyReservationsFragment;
import com.lksnext.ParkingXAbaunz.view.fragments.NewReservationFragment;
import com.lksnext.ParkingXAbaunz.view.fragments.ProfileFragment;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener el email del usuario desde el intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            userEmail = "";
        }

        // Configurar listener para el BottomNavigationView
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_new_reservation) {
                selectedFragment = NewReservationFragment.newInstance();
            } else if (itemId == R.id.nav_my_reservations) {
                selectedFragment = MyReservationsFragment.newInstance();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = ProfileFragment.newInstance(userEmail);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Establecer el fragmento inicial (Nueva Reserva)
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_new_reservation);
        }
    }
}