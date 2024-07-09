package com.toda.todamoon_v1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v1.Driver.DriverMainUI;
import com.toda.todamoon_v1.Driver.LoginDriver;
import com.toda.todamoon_v1.Passenger.LoginPassenger;
import com.toda.todamoon_v1.Passenger.PassengerMainUI;

public class FrontPage extends AppCompatActivity {

    private Button btn_go_to_pass_login, btn_go_to_driver_login;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_front_page);

        initializeView();
        setButtonListeners();

        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // If the user is already signed in, proceed to the appropriate main UI
            String userId = currentUser.getUid();
            //getUserRoleFromFirestore(userId);
        } else {
            // If no user is signed in, show the login/register options
            // Your existing code for showing login/register options goes here
        }


    }

    private void getUserRoleFromFirestore(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Passenger".equals(role)) {
                            startActivity(new Intent(FrontPage.this, PassengerMainUI.class));
                        } else if ("Driver".equals(role)) {
                            startActivity(new Intent(FrontPage.this, DriverMainUI.class));
                        } else {
                            // Unknown role, handle accordingly
                            // For example, show an error message or redirect to a default page
                            Toast.makeText(FrontPage.this, "Unknown user role", Toast.LENGTH_SHORT).show();
                        }
                        finish(); // Finish the login activity
                    } else {
                        // User document doesn't exist, handle accordingly
                        // For example, show an error message or redirect to a default page
                        Toast.makeText(FrontPage.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error fetching user data from Firestore, handle accordingly
                    // For example, show an error message or redirect to a default page
                    Toast.makeText(FrontPage.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeView() {
        btn_go_to_pass_login = findViewById(R.id.btnGoToPassLogin);
        btn_go_to_driver_login = findViewById(R.id.btnGoToDriverLogin);
    }

    private void setButtonListeners() {

        btn_go_to_pass_login.setOnClickListener(v -> {
            Intent intent = new Intent(FrontPage.this, LoginPassenger.class);
            startActivity(intent);
        });

        btn_go_to_driver_login.setOnClickListener(v -> {
            Intent intent = new Intent(FrontPage.this, LoginDriver.class);
            startActivity(intent);
        });

    }
}