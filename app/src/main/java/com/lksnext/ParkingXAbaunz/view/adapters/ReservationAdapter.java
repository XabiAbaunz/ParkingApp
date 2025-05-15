package com.lksnext.ParkingXAbaunz.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.domain.Reserva;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private final List<Reserva> reservations;
    private final OnReservationClickListener listener;

    public interface OnReservationClickListener {
        void onReservationClick(Reserva reserva);
    }

    public ReservationAdapter(List<Reserva> reservations, OnReservationClickListener listener) {
        this.reservations = reservations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reserva reserva = reservations.get(position);
        holder.bind(reserva, listener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {

        private final TextView dateText;
        private final TextView typeText;
        private final TextView timeText;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            typeText = itemView.findViewById(R.id.typeText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        public void bind(Reserva reserva, OnReservationClickListener listener) {
            String fechaOriginal = reserva.getFecha();
            String fechaFormateada = formatearFecha(fechaOriginal);
            dateText.setText(fechaFormateada);

            typeText.setText(reserva.getPlazaId().getTipo());

            long horaInicio = reserva.getHoraInicio();
            long horaFin = reserva.getHoraFin();

            int inicioHoras = (int)(horaInicio / 3600);
            int inicioMinutos = (int)((horaInicio % 3600) / 60);
            int finHoras = (int)(horaFin / 3600);
            int finMinutos = (int)((horaFin % 3600) / 60);

            String horasTexto = String.format("%02d:%02d - %02d:%02d",
                    inicioHoras, inicioMinutos, finHoras, finMinutos);
            timeText.setText(horasTexto);

            itemView.setOnClickListener(v -> listener.onReservationClick(reserva));
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
    }
}