package com.toda.todamoon_v1.Driver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.toda.todamoon_v1.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class DriverSettingsFragment extends Fragment {

    private TextView txtName, txtEmail;
    private CircleImageView imageProfile;
    private View logLayout;
    private static final String ARG_EMAIL = "email";
    private static final String ARG_NAME = "name";
    private static final String ARG_PROFILE_URI = "profileUri";
    private Drawable profileImg = null;


    public DriverSettingsFragment() {
        // Required empty public constructor
    }


    public static DriverSettingsFragment newInstance(String email, String name) {
        DriverSettingsFragment fragment = new DriverSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint({"MissingInflatedId", "UseCompatLoadingForDrawables"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.driver_settings_fragment, container, false);

        profileImg = getResources().getDrawable(R.mipmap.ic_launcher_round);

        // Initialize views
        txtName = view.findViewById(R.id.profNameDriver);
        txtEmail = view.findViewById(R.id.profEmailDriver);
        imageProfile = view.findViewById(R.id.profileImgDriver);
        logLayout = view.findViewById(R.id.logoutLayoutDriver);

        // Set logout button click listener
        logLayout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Retrieve user data based on role
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString(ARG_EMAIL);
            String name = args.getString(ARG_NAME);
            String profileUri = args.getString(ARG_PROFILE_URI);

            // Set user data to views
            txtName.setText(name);
            txtEmail.setText(email);
            Glide.with(DriverSettingsFragment.this).load(profileImg).into(imageProfile);
        }

        return view;
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", null)
                .show();
    }
    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        // Navigate back to the login screen
        Intent intent = new Intent(getActivity(), LoginDriver.class);
        startActivity(intent);
        getActivity().finish(); // Close the current activity
    }
}