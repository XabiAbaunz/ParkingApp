package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentNewReservationBinding;
import com.lksnext.ParkingXAbaunz.domain.Hora;
import com.lksnext.ParkingXAbaunz.domain.Plaza;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewReservationFragment extends Fragment {

    private FragmentNewReservationBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isStartTimeSelected = false;
    private boolean isEndTimeSelected = false;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

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

        setupSpinner();

        // Configurar los listeners de los botones
        binding.selectDateButton.setOnClickListener(v -> showDatePicker());
        binding.selectStartTimeButton.setOnClickListener(v -> showTimePicker(true));
        binding.selectEndTimeButton.setOnClickListener(v -> showTimePicker(false));
        binding.confirmReservationButton.setOnClickListener(v -> confirmReservation());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plaza_types,
                R.layout.spinner_item_selected
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        binding.typeSpinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    isDateSelected = true;

                    binding.selectDateButton.setText("Fecha: " + dateFormatter.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Establecer fecha mínima (hoy)
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
        // Validar que todos los campos estén completos
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

        // Validar que la hora de fin sea posterior a la hora de inicio
        if (endTime.before(startTime)) {
            Toast.makeText(requireContext(), "La hora de fin debe ser posterior a la hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el tipo de plaza seleccionado
        String plazaType = binding.typeSpinner.getSelectedItem().toString();

        // Crear objetos para la reserva
        Plaza plaza = new Plaza(1, plazaType);

        // Convertir a segundos desde medianoche para el formato Hora
        int startSeconds = startTime.get(Calendar.HOUR_OF_DAY) * 3600 + startTime.get(Calendar.MINUTE) * 60;
        int endSeconds = endTime.get(Calendar.HOUR_OF_DAY) * 3600 + endTime.get(Calendar.MINUTE) * 60;
        Hora hora = new Hora(startSeconds, endSeconds);

        /* Crear la reserva (aquí usaríamos un ID real y el email del usuario actual)
        Reserva reserva = new Reserva(
                dateFormatter.format(selectedDate.getTime()),
                "usuario@example.com", // Este sería el email del usuario actual
                "temp-" + System.currentTimeMillis(), // En producción se generaría un ID real
                plaza,
                hora
        ); */

        // Aquí se guardaría la reserva en la base de datos
        // Por ahora solo mostraremos un mensaje de éxito
        Toast.makeText(requireContext(), "Reserva confirmada para " + plazaType + " el día "
                + dateFormatter.format(selectedDate.getTime()), Toast.LENGTH_LONG).show();

        // Resetear todos los campos después de confirmar
        resetFields();
    }

    /**
     * Resetea todos los campos del formulario a su estado inicial
     */
    private void resetFields() {
        // Resetear spinner al primer elemento
        binding.typeSpinner.setSelection(0);

        // Resetear textos de botones
        binding.selectDateButton.setText("Seleccionar Fecha");
        binding.selectStartTimeButton.setText("Seleccionar Hora Inicio");
        binding.selectEndTimeButton.setText("Seleccionar Hora Fin");

        // Resetear variables de control
        isDateSelected = false;
        isStartTimeSelected = false;
        isEndTimeSelected = false;

        // Resetear calendarios a la hora actual
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