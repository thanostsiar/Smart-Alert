package com.unipi.tsiaras.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SignIn extends AppCompatActivity {

    Button btn_sign_up;
    Button btn_sign_in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        btn_sign_up = findViewById(R.id.button_signup);
        btn_sign_in = findViewById(R.id.button_signin);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        btn_sign_up.setOnClickListener(v -> {
            OpenSignUp();
        });


    }
    public void OpenSignUp(){
        Intent intent = new Intent(getApplicationContext(), SignUp.class);
        startActivity(intent);
    }
}