package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingXAbaunz.databinding.FragmentMyReservationsBinding;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.view.adapters.ReservationAdapter;
import com.lksnext.ParkingXAbaunz.viewmodel.MyReservationsViewModel;

import java.util.ArrayList;
import java.util.List;

public class MyReservationsFragment extends Fragment implements ReservationAdapter.OnReservationClickListener {

    private FragmentMyReservationsBinding binding;
    private MyReservationsViewModel viewModel;
    private ReservationAdapter futureAdapter;
    private ReservationAdapter pastAdapter;
    private RecyclerView futureReservationsRecyclerView;
    private RecyclerView pastReservationsRecyclerView;
    private ProgressBar progressBar;
    private TextView futureEmptyText;
    private TextView pastEmptyText;

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

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(MyReservationsViewModel.class);

        futureReservationsRecyclerView = binding.futureReservationsRecyclerView;
        pastReservationsRecyclerView = binding.pastReservationsRecyclerView;
        progressBar = binding.progressBar;
        futureEmptyText = binding.futureEmptyText;
        pastEmptyText = binding.pastEmptyText;

        setupRecyclerViews();
        setupObservers();
        viewModel.loadReservations();
    }

    private void setupRecyclerViews() {
        futureAdapter = new ReservationAdapter(new ArrayList<>(), this);
        futureReservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        futureReservationsRecyclerView.setAdapter(futureAdapter);

        pastAdapter = new ReservationAdapter(new ArrayList<>(), this);
        pastReservationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pastReservationsRecyclerView.setAdapter(pastAdapter);
    }

    private void setupObservers() {
        viewModel.getFutureReservationsLiveData().observe(getViewLifecycleOwner(), this::updateFutureReservations);
        viewModel.getPastReservationsLiveData().observe(getViewLifecycleOwner(), this::updatePastReservations);

        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading ->
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFutureReservations(List<Reserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            futureEmptyText.setVisibility(View.VISIBLE);
            futureReservationsRecyclerView.setVisibility(View.GONE);
        } else {
            futureEmptyText.setVisibility(View.GONE);
            futureReservationsRecyclerView.setVisibility(View.VISIBLE);
            futureAdapter = new ReservationAdapter(reservas, this);
            futureReservationsRecyclerView.setAdapter(futureAdapter);
        }
    }

    private void updatePastReservations(List<Reserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            pastEmptyText.setVisibility(View.VISIBLE);
            pastReservationsRecyclerView.setVisibility(View.GONE);
        } else {
            pastEmptyText.setVisibility(View.GONE);
            pastReservationsRecyclerView.setVisibility(View.VISIBLE);
            pastAdapter = new ReservationAdapter(reservas, this);
            pastReservationsRecyclerView.setAdapter(pastAdapter);
        }
    }

    @Override
    public void onReservationClick(Reserva reserva) {
        List<Reserva> futureReservations = viewModel.getFutureReservationsLiveData().getValue();
        boolean isFutureReservation = futureReservations != null && futureReservations.contains(reserva);

        if (isFutureReservation) {
            showReservationOptions(reserva);
        } else {
            showReservationDetails(reserva);
        }
    }

    private void showReservationOptions(Reserva reserva) {
        String[] options = {"Ver detalles", "Editar reserva", "Eliminar reserva"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Opciones de reserva");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showReservationDetails(reserva);
                    break;
                case 1:
                    editReservation(reserva);
                    break;
                case 2:
                    confirmDeleteReservation(reserva);
                    break;
            }
        });
        builder.show();
    }

    private void confirmDeleteReservation(Reserva reserva) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Reserva")
                .setMessage("¿Estás seguro de que quieres eliminar esta reserva?")
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.deleteReservation(reserva))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showReservationDetails(Reserva reserva) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Detalles de la reserva");

        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Fecha: ").append(formatearFecha(reserva.getFecha())).append("\n\n");

        if (reserva.getCoche() != null) {
            detailsBuilder.append("Coche: ").append(reserva.getCoche().getMarca())
                    .append(" ").append(reserva.getCoche().getModelo())
                    .append(" (").append(reserva.getCoche().getMatricula()).append(")\n\n");
        }

        if (reserva.getPlazaId() != null) {
            detailsBuilder.append("Tipo de plaza: ").append(reserva.getPlazaId().getTipo()).append("\n\n");
        }

        detailsBuilder.append("Horario: ").append(formatearHora(reserva.getHoraInicio()))
                .append(" - ").append(formatearHora(reserva.getHoraFin()));

        builder.setMessage(detailsBuilder.toString());
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void editReservation(Reserva reserva) {
        EditReservationDialogFragment dialogFragment = EditReservationDialogFragment.newInstance(reserva);
        dialogFragment.show(getParentFragmentManager(), "EditReservationDialog");
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.length() != 10) {
            return fechaOriginal;
        }

        try {
            String anio = fechaOriginal.substring(0, 4);
            String mes = fechaOriginal.substring(5, 7);
            String dia = fechaOriginal.substring(8, 10);

            return dia + "/" + mes + "/" + anio;
        } catch (Exception e) {
            return fechaOriginal;
        }
    }

    private String formatearHora(long segundos) {
        int horas = (int)(segundos / 3600);
        int minutos = (int)((segundos % 3600) / 60);
        return String.format("%02d:%02d", horas, minutos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}