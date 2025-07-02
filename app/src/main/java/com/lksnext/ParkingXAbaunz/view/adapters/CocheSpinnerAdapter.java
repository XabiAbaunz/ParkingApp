package com.lksnext.ParkingXAbaunz.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.domain.Coche;

import java.util.List;

public class CocheSpinnerAdapter extends ArrayAdapter<Coche> {

    private final LayoutInflater inflater;

    public CocheSpinnerAdapter(@NonNull Context context, @NonNull List<Coche> coches) {
        super(context, R.layout.spinner_item_selected, coches);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createDropDownItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_selected, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        Coche coche = getItem(position);

        if (coche != null) {
            textView.setText(coche.getMarca() + " " + coche.getModelo() + " (" + coche.getMatricula() + ")");
        }

        return view;
    }

    private View createDropDownItemView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_dropdown, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        Coche coche = getItem(position);

        if (coche != null) {
            textView.setText(coche.getMarca() + " " + coche.getModelo() + " (" + coche.getMatricula() + ")");
        }

        return view;
    }
}