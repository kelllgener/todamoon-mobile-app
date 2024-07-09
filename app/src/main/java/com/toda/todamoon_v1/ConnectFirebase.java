package com.toda.todamoon_v1;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ConnectFirebase {
    public static FirebaseAuth getFirebaseAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseDatabase getFirebaseDatabaseInstance() {
        return FirebaseDatabase.getInstance();
    }

    public static GoogleSignInOptions getGoogleSignInOptions(String clientId) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
    }
}



