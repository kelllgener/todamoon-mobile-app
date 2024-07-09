package com.toda.todamoon_v1.Passenger;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.toda.todamoon_v1.R;

public class RegisterPassenger extends AppCompatActivity {
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_passenger);

        initializeView();
        setUpButtonListener();

    }

    private void initializeView() {
        login = findViewById(R.id.txtLogin);
    }
    private void setUpButtonListener() {

        login.setOnClickListener(v -> finish());

    }

}