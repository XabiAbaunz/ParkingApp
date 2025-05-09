package com.lksnext.ParkingXAbaunz.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.lksnext.ParkingXAbaunz.R;
import com.lksnext.ParkingXAbaunz.databinding.FragmentProfileBinding;
import com.lksnext.ParkingXAbaunz.view.LoginActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userEmail = getArguments().getString("USER_EMAIL", "");
            binding.emailText.setText(userEmail);
        }

        binding.logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public static ProfileFragment newInstance(String userEmail) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("USER_EMAIL", userEmail);
        fragment.setArguments(args);
        return fragment;
    }
}