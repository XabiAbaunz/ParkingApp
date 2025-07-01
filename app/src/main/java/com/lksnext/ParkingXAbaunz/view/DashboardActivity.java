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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityDashboardBinding binding;
        String userEmail;

        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            userEmail = "";
        }

        String finalUserEmail = userEmail;
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_new_reservation) {
                selectedFragment = NewReservationFragment.newInstance();
            } else if (itemId == R.id.nav_my_reservations) {
                selectedFragment = MyReservationsFragment.newInstance();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = ProfileFragment.newInstance(finalUserEmail);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_my_reservations);
        }
    }
}