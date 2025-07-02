package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentEditReservationBinding;
import com.lksnext.ParkingXAbaunz.domain.CallbackWithData;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;
import com.lksnext.ParkingXAbaunz.view.adapters.CocheSpinnerAdapter;
import com.lksnext.ParkingXAbaunz.viewmodel.MyReservationsViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditReservationDialogFragment extends DialogFragment {

    private FragmentEditReservationBinding binding;
    private MyReservationsViewModel viewModel;
    private Reserva reserva;
    private List<Coche> coches = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    private long startTimeSeconds;
    private long endTimeSeconds;

    public static EditReservationDialogFragment newInstance(Reserva reserva) {
        EditReservationDialogFragment fragment = new EditReservationDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("reserva", reserva);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        if (getArguments() != null) {
            reserva = (Reserva) getArguments().getSerializable("reserva");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditReservationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MyReservationsViewModel.class);

        if (reserva != null) {
            try {
                Date date = dateFormat.parse(reserva.getFecha());
                if (date != null) {
                    calendar.setTime(date);
                    updateDateButton();
                }

                startTimeSeconds = reserva.getHoraInicio();
                endTimeSeconds = reserva.getHoraFin();
                updateTimeButtons();
                loadCochesAndPlazas();

            } catch (ParseException e) {
                Toast.makeText(getContext(), "Error al cargar los datos de la reserva", Toast.LENGTH_SHORT).show();
            }
        }

        setupListeners();
    }

    private void loadCochesAndPlazas() {
        viewModel.getCoches((CallbackWithData<List<Coche>>) new CallbackWithData<List<Coche>>() {
            @Override
            public void onSuccess(List<Coche> result) {
                coches = result;
                setupCarSpinner();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error al cargar los coches: " + error, Toast.LENGTH_SHORT).show();
                if (reserva.getCoche() != null && !coches.contains(reserva.getCoche())) {
                    coches.add(reserva.getCoche());
                }
                setupCarSpinner();
            }
        });

        String[] plazaTypes = getResources().getStringArray(R.array.plaza_types);
        ArrayAdapter<String> plazaAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item_dropdown,
                android.R.id.text1,
                plazaTypes);
        plazaAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        binding.plazaSpinner.setAdapter(plazaAdapter);

        if (reserva.getPlazaId() != null) {
            String currentType = reserva.getPlazaId().getTipo();
            for (int i = 0; i < plazaTypes.length; i++) {
                if (plazaTypes[i].equals(currentType)) {
                    binding.plazaSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupCarSpinner() {
        CocheSpinnerAdapter cocheAdapter = new CocheSpinnerAdapter(requireContext(), coches);
        binding.carSpinner.setAdapter(cocheAdapter);

        if (reserva.getCoche() != null) {
            for (int i = 0; i < coches.size(); i++) {
                if (coches.get(i).getMatricula().equals(reserva.getCoche().getMatricula())) {
                    binding.carSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupListeners() {
        binding.dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateButton();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        binding.startTimeButton.setOnClickListener(v -> {
            int hour = (int) (startTimeSeconds / 3600);
            int minute = (int) ((startTimeSeconds % 3600) / 60);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minuteOfHour) -> {
                        startTimeSeconds = hourOfDay * 3600L + minuteOfHour * 60L;
                        updateTimeButtons();
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        binding.endTimeButton.setOnClickListener(v -> {
            int hour = (int) (endTimeSeconds / 3600);
            int minute = (int) ((endTimeSeconds % 3600) / 60);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minuteOfHour) -> {
                        endTimeSeconds = hourOfDay * 3600L + minuteOfHour * 60L;
                        updateTimeButtons();
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        binding.saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                updateReservation();
            }
        });

        binding.cancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateDateButton() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.dateButton.setText(displayFormat.format(calendar.getTime()));
    }

    private void updateTimeButtons() {
        binding.startTimeButton.setText(formatTime(startTimeSeconds));
        binding.endTimeButton.setText(formatTime(endTimeSeconds));
    }

    private String formatTime(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    private boolean validateForm() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (calendar.before(today)) {
            Toast.makeText(getContext(), "La fecha debe ser igual o posterior a hoy", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endTimeSeconds <= startTimeSeconds) {
            Toast.makeText(getContext(), "La hora de fin debe ser posterior a la hora de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.carSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Debes seleccionar un coche", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateReservation() {
        reserva.setFecha(dateFormat.format(calendar.getTime()));
        reserva.setHoraInicio(startTimeSeconds);
        reserva.setHoraFin(endTimeSeconds);
        reserva.setCoche((Coche) binding.carSpinner.getSelectedItem());

        String selectedPlazaType = binding.plazaSpinner.getSelectedItem().toString();
        if (reserva.getPlazaId() == null) {
            reserva.setPlazaId(new Plaza());
        }
        reserva.getPlazaId().setTipo(selectedPlazaType);

        viewModel.updateReservation(reserva);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}