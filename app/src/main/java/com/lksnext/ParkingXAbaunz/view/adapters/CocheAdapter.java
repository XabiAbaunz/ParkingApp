package com.lksnext.ParkingXAbaunz.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.domain.Coche;

import java.util.List;

public class CocheAdapter extends RecyclerView.Adapter<CocheAdapter.CocheViewHolder> {

    private final List<Coche> coches;
    private final OnCocheDeleteListener listener;

    public interface OnCocheDeleteListener {
        void onCocheDelete(Coche coche);
    }

    public CocheAdapter(List<Coche> coches, OnCocheDeleteListener listener) {
        this.coches = coches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CocheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coche, parent, false);
        return new CocheViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CocheViewHolder holder, int position) {
        Coche coche = coches.get(position);
        holder.bind(coche, listener);
    }

    @Override
    public int getItemCount() {
        return coches.size();
    }

    static class CocheViewHolder extends RecyclerView.ViewHolder {

        private final TextView matriculaText;
        private final TextView marcaModeloText;
        private final Button deleteButton;

        public CocheViewHolder(@NonNull View itemView) {
            super(itemView);
            matriculaText = itemView.findViewById(R.id.matriculaText);
            marcaModeloText = itemView.findViewById(R.id.marcaModeloText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Coche coche, OnCocheDeleteListener listener) {
            matriculaText.setText(coche.getMatricula());
            marcaModeloText.setText(coche.getMarca() + " " + coche.getModelo());

            deleteButton.setOnClickListener(v -> listener.onCocheDelete(coche));
        }
    }
}