package com.toda.todamoon_v1.Passenger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v1.ConnectFirebase;
import com.toda.todamoon_v1.FrontPage;
import com.toda.todamoon_v1.R;

import java.util.HashMap;
import java.util.Map;

public class LoginPassenger extends AppCompatActivity {

    private ImageButton btn_back;
    private TextView signup_passenger;

    private Button googleAuth;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 20;
    private String clientId;
    private Dialog loadingDialog;

    private void initializeView() {

        // BACK BUTTON
        btn_back = findViewById(R.id.btnBack);
        signup_passenger = findViewById(R.id.signUpPassenger);

        //FIREBASE

        // Initialize Firebase Auth
        mAuth = ConnectFirebase.getFirebaseAuthInstance();

        // Initialize Google SignIn options
        clientId = "853318778029-21fdbfsqqaqlvdhjoimnejuq511g8gd6.apps.googleusercontent.com";
        gso = ConnectFirebase.getGoogleSignInOptions(clientId); // Force account selection prompt

        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Find Google Auth button by ID
        googleAuth = findViewById(R.id.btnGoogle);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_passenger);

        initializeView();
        setButtonListener();
        setupLoadingDialog();

    }
    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.activity_progress_bar);
        loadingDialog.setCancelable(false);
    }
    private void setButtonListener() {

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, FrontPage.class);
            startActivity(intent);
        });

        signup_passenger.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, RegisterPassenger.class);
            startActivity(intent);
        });

        // Set onClickListener for Google Auth button
        googleAuth.setOnClickListener(v -> signInWithGoogle());

    }
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI accordingly
                Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        loadingDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        loadingDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginPassenger.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, save user data to Firestore
            String userId = user.getUid();
            String email = user.getEmail();
            String name = user.getDisplayName();
            String profileUri = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
            String role = "Passenger";

            // Create a data object for Firestore
            Map<String, Object> firestoreData = new HashMap<>();
            firestoreData.put("email", email);
            firestoreData.put("name", name);
            firestoreData.put("profileUri", profileUri);
            firestoreData.put("role", role);

            // Get a reference to the Firestore database
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Add the user data to the "users" collection in Firestore
            db.collection("users").document(userId).set(firestoreData)
                    .addOnSuccessListener(aVoid -> {
                        // Data saved successfully in Firestore
                        // Save user data to Realtime Database
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("passenger_google");

                        // Create a data object for Realtime Database
                        PassengerGetterSetter newPassenger = new PassengerGetterSetter(userId, name, email, profileUri, role);

                        // Save the user to the Realtime Database
                        usersRef.child(userId).setValue(newPassenger)
                                .addOnSuccessListener(aVoid1 -> {
                                    // Data saved successfully in Realtime Database, navigate to home page
                                    Intent intent = new Intent(LoginPassenger.this, PassengerMainUI.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("name", name);
                                    intent.putExtra("profileUri", profileUri);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Data failed to save in Realtime Database
                                    Toast.makeText(LoginPassenger.this, "Failed to save user data to Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Data failed to save in Firestore
                        Toast.makeText(LoginPassenger.this, "Failed to save user data to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User is signed out
            // TODO: Handle sign-out state
        }
    }


}