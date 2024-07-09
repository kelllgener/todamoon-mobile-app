package com.toda.todamoon_v1.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.toda.todamoon_v1.R;
import com.toda.todamoon_v1.databinding.ActivityPassengerMainUiBinding;

public class PassengerMainUI extends AppCompatActivity {

    // VIEWS
    private View homeView;
    private View categoryView;
    private View settingsView;
    private View profileView;

    // PROFILE
    private View SettingsFrag;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ActivityPassengerMainUiBinding binding;

    private void initializeView() {
        SettingsFrag = LayoutInflater.from(this).inflate(R.layout.passenger_settings_fragment, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger_main_ui);

        binding = ActivityPassengerMainUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new PassengerHomeFragment());

        initializeView();
        navigateListener();

    }

    private void navigateListener() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.menu_passenger_home) {
                replaceFragment(new PassengerHomeFragment());
            } else if (itemId == R.id.menu_passenger_settings) {
                // Retrieve user data from intent
                String email = getIntent().getStringExtra("email");
                String name = getIntent().getStringExtra("name");
                String profileUri = getIntent().getStringExtra("profileUri");

                // Pass user data to ProfileFragment
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bundle.putString("name", name);
                bundle.putString("profileUri", profileUri);

                PassengerSettingsFragment settingsFragment = PassengerSettingsFragment.newInstance(email, name, profileUri);

                // Replace the current fragment with ProfileFragment
                replaceFragment(settingsFragment);

            } else {
                throw new IllegalStateException("Unexpected value: " + itemId);
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();


    }
}