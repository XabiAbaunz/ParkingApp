package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentNewReservationBinding;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.view.adapters.CocheSpinnerAdapter;
import com.lksnext.ParkingXAbaunz.viewmodel.NewReservationViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewReservationFragment extends Fragment {

    private FragmentNewReservationBinding binding;
    private NewReservationViewModel viewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isStartTimeSelected = false;
    private boolean isEndTimeSelected = false;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Coche selectedCoche;
    private List<Coche> cochesList = new ArrayList<>();

    public static NewReservationFragment newInstance() {
        return new NewReservationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewReservationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NewReservationViewModel.class);

        setupSpinners();
        setupObservers();
        setupClickListeners();

        viewModel.loadCoches();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plaza_types,
                R.layout.spinner_item_selected
        );
        tipoAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        binding.typeSpinner.setAdapter(tipoAdapter);

        CocheSpinnerAdapter cocheAdapter = new CocheSpinnerAdapter(requireContext(), cochesList);
        binding.cocheSpinner.setAdapter(cocheAdapter);

        binding.cocheSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCoche = (Coche) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCoche = null;
            }
        });
    }

    private void setupObservers() {
        viewModel.getCochesLiveData().observe(getViewLifecycleOwner(), coches -> {
            cochesList.clear();
            if (coches != null && !coches.isEmpty()) {
                cochesList.addAll(coches);
                CocheSpinnerAdapter adapter = (CocheSpinnerAdapter) binding.cocheSpinner.getAdapter();
                adapter.notifyDataSetChanged();

                if (!cochesList.isEmpty()) {
                    selectedCoche = cochesList.get(0);
                }
            } else {
                Toast.makeText(requireContext(), "No tienes coches registrados. Por favor, añade un coche primero.", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(requireContext(), success, Toast.LENGTH_LONG).show();
                resetFields();
            }
        });

        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupClickListeners() {
        binding.selectDateButton.setOnClickListener(v -> showDatePicker());
        binding.selectStartTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.selectEndTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.confirmReservationButton.setOnClickListener(v -> confirmReservation());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    isDateSelected = true;
                    binding.selectDateButton.setText("Fecha: " + displayDateFormatter.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        Calendar today = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        final Calendar calendar = isStartTime ? startTime : endTime;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    if (isStartTime) {
                        isStartTimeSelected = true;
                        binding.selectStartTimeButton.setText("Hora inicio: " + timeFormatter.format(calendar.getTime()));
                    } else {
                        isEndTimeSelected = true;
                        binding.selectEndTimeButton.setText("Hora fin: " + timeFormatter.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void confirmReservation() {
        if (selectedCoche == null) {
            Toast.makeText(requireContext(), "Por favor selecciona un coche", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateSelected) {
            Toast.makeText(requireContext(), "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isStartTimeSelected) {
            Toast.makeText(requireContext(), "Por favor selecciona hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEndTimeSelected) {
            Toast.makeText(requireContext(), "Por favor selecciona hora de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endTime.before(startTime)) {
            Toast.makeText(requireContext(), "La hora de fin debe ser posterior a la hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String plazaType = binding.typeSpinner.getSelectedItem().toString();
        int startSeconds = startTime.get(Calendar.HOUR_OF_DAY) * 3600 + startTime.get(Calendar.MINUTE) * 60;
        int endSeconds = endTime.get(Calendar.HOUR_OF_DAY) * 3600 + endTime.get(Calendar.MINUTE) * 60;

        viewModel.saveReservation(
                dateFormatter.format(selectedDate.getTime()),
                selectedCoche,
                plazaType,
                startSeconds,
                endSeconds
        );
    }

    private void resetFields() {
        binding.typeSpinner.setSelection(0);

        if (!cochesList.isEmpty()) {
            binding.cocheSpinner.setSelection(0);
            selectedCoche = cochesList.get(0);
        } else {
            selectedCoche = null;
        }

        binding.selectDateButton.setText("Seleccionar Fecha");
        binding.selectStartTimeButton.setText("Seleccionar Hora Inicio");
        binding.selectEndTimeButton.setText("Seleccionar Hora Fin");

        isDateSelected = false;
        isStartTimeSelected = false;
        isEndTimeSelected = false;

        selectedDate = Calendar.getInstance();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}