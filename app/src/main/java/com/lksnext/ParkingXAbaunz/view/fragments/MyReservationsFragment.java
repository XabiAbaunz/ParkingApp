package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentMyReservationsBinding;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.view.adapters.ReservationAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyReservationsFragment extends Fragment {

    private FragmentMyReservationsBinding binding;
    private ReservationAdapter futureAdapter, pastAdapter;
    private List<Reserva> futureReservations;
    private List<Reserva> pastReservations;

    public static MyReservationsFragment newInstance() {
        return new MyReservationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        futureReservations = getFutureReservations();
        pastReservations = getPastReservations();

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        futureAdapter = new ReservationAdapter(futureReservations, this::onReservationClick);
        pastAdapter = new ReservationAdapter(pastReservations, this::onReservationClick);

        binding.futureReservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.futureReservationsRecyclerView.setAdapter(futureAdapter);

        binding.pastReservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pastReservationsRecyclerView.setAdapter(pastAdapter);
    }

    private List<Reserva> getFutureReservations() {
        List<Reserva> reservations = new ArrayList<>();

        reservations.add(new Reserva("2025-05-16", "usuario1@example.com", "1",
                new Plaza(101, "Normal"), new Hora(9 * 3600, 11 * 3600)));
        reservations.add(new Reserva("2025-05-20", "usuario2@example.com", "2",
                new Plaza(102, "Eléctrico"), new Hora(14 * 3600, 16 * 3600)));
        reservations.add(new Reserva("2025-05-25", "usuario3@example.com", "3",
                new Plaza(103, "Motocicleta"), new Hora(8 * 3600, 10 * 3600)));

        reservations.add(new Reserva("2025-06-01", "xabi@example.com", "7",
                new Plaza(107, "Normal"), new Hora(10 * 3600, 12 * 3600)));
        reservations.add(new Reserva("2025-06-05", "xabi@example.com", "8",
                new Plaza(108, "Eléctrico"), new Hora(13 * 3600, 15 * 3600)));
        reservations.add(new Reserva("2025-06-10", "xabi@example.com", "9",
                new Plaza(109, "Normal"), new Hora(9 * 3600, 11 * 3600)));
        reservations.add(new Reserva("2025-06-15", "xabi@example.com", "10",
                new Plaza(110, "Minusválido"), new Hora(14 * 3600, 16 * 3600)));
        reservations.add(new Reserva("2025-06-20", "xabi@example.com", "11",
                new Plaza(111, "Normal"), new Hora(8 * 3600, 10 * 3600)));
        reservations.add(new Reserva("2025-06-25", "xabi@example.com", "12",
                new Plaza(112, "Eléctrico"), new Hora(11 * 3600, 13 * 3600)));
        reservations.add(new Reserva("2025-06-30", "xabi@example.com", "13",
                new Plaza(113, "Motocicleta"), new Hora(15 * 3600, 17 * 3600)));
        reservations.add(new Reserva("2025-07-05", "xabi@example.com", "14",
                new Plaza(114, "Normal"), new Hora(9 * 3600, 11 * 3600)));

        return reservations;
    }

    private List<Reserva> getPastReservations() {
        List<Reserva> reservations = new ArrayList<>();

        reservations.add(new Reserva("2025-05-01", "usuario4@example.com", "4",
                new Plaza(104, "Minusválido"), new Hora(10 * 3600, 12 * 3600)));
        reservations.add(new Reserva("2025-05-05", "usuario5@example.com", "5",
                new Plaza(105, "Normal"), new Hora(13 * 3600, 15 * 3600)));
        reservations.add(new Reserva("2025-05-10", "usuario6@example.com", "6",
                new Plaza(106, "Eléctrico"), new Hora(9 * 3600, 11 * 3600)));
        reservations.add(new Reserva("2025-05-14", "xabi@example.com", "15",
                new Plaza(115, "Normal"), new Hora(8 * 3600, 10 * 3600)));

        reservations.add(new Reserva("2025-04-01", "xabi@example.com", "16",
                new Plaza(116, "Eléctrico"), new Hora(11 * 3600, 13 * 3600)));
        reservations.add(new Reserva("2025-04-05", "xabi@example.com", "17",
                new Plaza(117, "Motocicleta"), new Hora(14 * 3600, 16 * 3600)));
        reservations.add(new Reserva("2025-04-10", "xabi@example.com", "18",
                new Plaza(118, "Minusválido"), new Hora(9 * 3600, 11 * 3600)));
        reservations.add(new Reserva("2025-04-15", "xabi@example.com", "19",
                new Plaza(119, "Normal"), new Hora(12 * 3600, 14 * 3600)));
        reservations.add(new Reserva("2025-04-20", "xabi@example.com", "20",
                new Plaza(120, "Eléctrico"), new Hora(15 * 3600, 17 * 3600)));
        reservations.add(new Reserva("2025-04-25", "xabi@example.com", "21",
                new Plaza(121, "Normal"), new Hora(8 * 3600, 10 * 3600)));

        return reservations;
    }

    private void onReservationClick(Reserva reserva) {
        new AlertDialog.Builder(getContext())
                .setTitle("Opciones de Reserva")
                .setMessage("¿Qué quieres hacer con esta reserva?")
                .setPositiveButton("Editar", (dialog, which) -> {
                    Toast.makeText(getContext(), "Editar reserva", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Eliminar", (dialog, which) -> {
                    boolean removed = false;

                    if (futureReservations.contains(reserva)) {
                        futureReservations.remove(reserva);
                        futureAdapter.notifyDataSetChanged();
                        removed = true;
                    } else if (pastReservations.contains(reserva)) {
                        pastReservations.remove(reserva);
                        pastAdapter.notifyDataSetChanged();
                        removed = true;
                    }

                    if (removed) {
                        Toast.makeText(getContext(), "Reserva eliminada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}