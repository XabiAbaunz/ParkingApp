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


        return reservations;
    }

    private List<Reserva> getPastReservations() {
        List<Reserva> reservations = new ArrayList<>();

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