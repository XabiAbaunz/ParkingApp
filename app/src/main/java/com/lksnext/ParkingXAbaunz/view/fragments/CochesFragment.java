package com.lksnext.ParkingXAbaunz.view.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentCochesBinding;
import com.lksnext.ParkingXAbaunz.domain.Coche;
import com.lksnext.ParkingXAbaunz.view.adapters.CocheAdapter;
import com.lksnext.ParkingXAbaunz.viewmodel.CochesViewModel;

import java.util.ArrayList;

public class CochesFragment extends Fragment implements CocheAdapter.OnCocheDeleteListener {

    private FragmentCochesBinding binding;
    private CochesViewModel viewModel;
    private CocheAdapter adapter;

    public static CochesFragment newInstance() {
        return new CochesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCochesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CochesViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        viewModel.loadCoches();
    }

    private void setupRecyclerView() {
        adapter = new CocheAdapter(new ArrayList<>(), this);
        binding.cochesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.cochesRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCochesLiveData().observe(getViewLifecycleOwner(), coches -> {
            adapter = new CocheAdapter(coches, this);
            binding.cochesRecyclerView.setAdapter(adapter);

            if (coches.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
                binding.cochesRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                binding.cochesRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

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

    private void setupClickListeners() {
        binding.addCarButton.setOnClickListener(v -> showAddCarDialog());
    }

    private void showAddCarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Añadir Coche");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText matriculaInput = new EditText(getContext());
        matriculaInput.setHint("Matrícula");
        matriculaInput.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(matriculaInput);

        final EditText marcaInput = new EditText(getContext());
        marcaInput.setHint("Marca");
        marcaInput.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(marcaInput);

        final EditText modeloInput = new EditText(getContext());
        modeloInput.setHint("Modelo");
        modeloInput.setTextColor(getResources().getColor(android.R.color.black));
        layout.addView(modeloInput);

        builder.setView(layout);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String matricula = matriculaInput.getText().toString();
            String marca = marcaInput.getText().toString();
            String modelo = modeloInput.getText().toString();

            viewModel.addCoche(matricula, marca, modelo);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onCocheDelete(Coche coche) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Coche")
                .setMessage("¿Estás seguro de que quieres eliminar el coche " + coche.getMatricula() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.deleteCoche(coche.getMatricula());
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}