package com.lksnext.ParkingXAbaunz.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.ActivityDashboardBinding;
import com.lksnext.ParkingXAbaunz.utils.PermissionHelper;
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

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            userEmail = "";
        }

        requestAllPermissions();

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

    private void requestAllPermissions() {
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
        } else {
            checkExactAlarmPermission();
        }
    }

    private void checkExactAlarmPermission() {
        if (!PermissionHelper.hasExactAlarmPermission(this)) {
            showExactAlarmPermissionDialog();
        }
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos de Recordatorios")
                .setMessage("Para enviarte recordatorios precisos de tus reservas (30 minutos antes del inicio y 15 minutos antes del final), necesitamos permiso para programar notificaciones exactas.\n\n¿Deseas conceder este permiso?")
                .setPositiveButton("Conceder Permiso", (dialog, which) -> {
                    PermissionHelper.requestExactAlarmPermission(this);
                })
                .setNegativeButton("Ahora No", (dialog, which) -> {
                    showPermissionDeniedInfo();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionDeniedInfo() {
        new AlertDialog.Builder(this)
                .setTitle("Recordatorios Limitados")
                .setMessage("Sin este permiso, no podremos enviarte recordatorios exactos de tus reservas.\n\nPuedes activarlo más tarde en:\nConfiguración > Aplicaciones > ParkingX > Permisos especiales > Alarmas y recordatorios")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void showPermissionGrantedInfo() {
        new AlertDialog.Builder(this)
                .setTitle("¡Perfecto!")
                .setMessage("Ahora recibirás recordatorios de tus reservas:\n\n• 30 minutos antes del inicio\n• 15 minutos antes del final")
                .setPositiveButton("Genial", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permiso de notificaciones concedido");
                checkExactAlarmPermission();
            } else {
                System.out.println("Permiso de notificaciones denegado");
                new AlertDialog.Builder(this)
                        .setTitle("Notificaciones Deshabilitadas")
                        .setMessage("Sin permisos de notificación, no podrás recibir recordatorios de tus reservas.\n\nPuedes habilitarlos más tarde en Configuración > Aplicaciones > ParkingX > Permisos.")
                        .setPositiveButton("Entendido", null)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PermissionHelper.EXACT_ALARM_PERMISSION_CODE) {
            if (PermissionHelper.hasExactAlarmPermission(this)) {
                showPermissionGrantedInfo();
            } else {
                showPermissionDeniedInfo();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionHelper.hasAllRequiredPermissions(this)) {
            System.out.println("Todos los permisos concedidos");
        }
    }
}