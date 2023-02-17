package com.unipi.tsiaras.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignIn extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText email, pass;
    Button btn_sign_up;
    Button btn_sign_in;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        email = findViewById(R.id.et_email);
        pass = findViewById(R.id.et_password);
        btn_sign_up = findViewById(R.id.button_signup);
        btn_sign_in = findViewById(R.id.button_signin);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        btn_sign_up.setOnClickListener(v -> {
            OpenSignUp();
        });


    }
    public void OpenSignUp(){
        Intent intent = new Intent(getApplicationContext(), SignUp.class);
        startActivity(intent);
    }

    public void signIn(View view){
        String emailTxt = email.getText().toString();
        String passTxt = pass.getText().toString();

        if (emailTxt.isEmpty() || passTxt.isEmpty()){
            Toast.makeText(this, "Please fill in the blanks!", Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(emailTxt, passTxt)
                    .addOnCompleteListener((task) ->{
                        if (task.isSuccessful()){
                            Intent intent;
                            if(emailTxt.contains("@gov.gr")){
                                intent = new Intent(this, EmployeeActivity.class);
                            }
                            else{
                                intent = new Intent(this, MainActivity.class);
                            }
                            startActivity(intent);
                            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}