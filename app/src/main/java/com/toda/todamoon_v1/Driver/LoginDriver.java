package com.toda.todamoon_v1.Driver;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v1.HashPassword;
import com.toda.todamoon_v1.R;

public class LoginDriver extends AppCompatActivity {

    private ImageView back;
    private TextView registerDriver;
    private Button buttonLogin;
    private TextInputEditText emailEditText, passwordEditText;
    private Dialog loadingDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_driver);
        initializeViews();
        setupListeners();
        setupLoadingDialog();


    }

    private void initializeViews() {
        back = findViewById(R.id.btn_back);
        registerDriver = findViewById(R.id.txtCreateAcc);
        buttonLogin = findViewById(R.id.btnLoginDriver);
        emailEditText = findViewById(R.id.editEmailDriver);
        passwordEditText = findViewById(R.id.editPasswordDriver);
        db = FirebaseFirestore.getInstance();
    }

    private void setupListeners() {
        back.setOnClickListener(v -> finish());

        registerDriver.setOnClickListener(v -> startActivity(new Intent(LoginDriver.this, RegisterDriver.class)));

        buttonLogin.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginDriver(email, password);
        });
    }

    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.activity_progress_bar);
        loadingDialog.setCancelable(false);
    }

    private void loginDriver(String email, String password) {

        HashPassword hashPass = new HashPassword();
        loadingDialog.show();

        // Hash the password before signing in
        String hashedPassword = hashPass.hashPassword(password);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);
                        Toast.makeText(LoginDriver.this, "Login successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginDriver.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String email = document.getString("email");
                                String name = document.getString("name");
                                String driverUid = document.getString("uid");
                                String tricycleNumber = document.getString("tricycleNumber");
                                //Double balance = document.getDouble("balance");

                                // Retrieve the QR code URL from Firebase Storage
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference qrCodeRef = storage.getReference().child("qrcodes/" + driverUid + ".png");

                                qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String qrCodeUrl = uri.toString();

                                    Intent intent = new Intent(LoginDriver.this, DriverMainUI.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("name", name);
                                    intent.putExtra("driverUid", driverUid);
                                    intent.putExtra("tricycleNumber", tricycleNumber);
                                    intent.putExtra("qrCodeUrl", qrCodeUrl); // Add QR code URL to intent
                                    //intent.putExtra("balance", balance != null ? balance : 0.00); // Add balance to intent
                                    startActivity(intent);
                                    finish();

                                }).addOnFailureListener(exception -> {
                                    Toast.makeText(LoginDriver.this, "Failed to retrieve QR code URL", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to retrieve QR code URL: ", exception);
                                });
                            } else {
                                Toast.makeText(LoginDriver.this, "No such user data exists", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginDriver.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
