package com.unipi.tsiaras.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toolbar;

public class SignUp extends AppCompatActivity {

    Button btn_sign_up2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btn_sign_up2 = findViewById(R.id.button_signup2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        btn_sign_up2.setOnClickListener(v -> {
            OpenSignIn();
        });
    }
    public void OpenSignIn(){
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }
}