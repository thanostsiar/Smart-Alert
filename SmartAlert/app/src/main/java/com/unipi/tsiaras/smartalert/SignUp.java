package com.unipi.tsiaras.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    EditText name, surname, phone, email, pass, conf_pass;
    FirebaseAuth mAuth;
    User user;
    FirebaseDatabase database;

    DatabaseReference reference;
    Button btn_sign_up2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.et_name);
        surname = findViewById(R.id.et_surname);
        phone = findViewById(R.id.et_phone);
        email = findViewById(R.id.et_email2);
        pass = findViewById(R.id.et_password2);
        conf_pass = findViewById(R.id.et_confirm_password);
        btn_sign_up2 = findViewById(R.id.button_signup2);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        btn_sign_up2.setOnClickListener(v -> {
            OpenSignIn();
        });
    }
    public void OpenSignIn(){
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }


    public void signUp(View view){

        // Get data from EditTexts into String variables.
        String nameTxt = name.getText().toString();
        String surnameTxt = surname.getText().toString();
        String phoneTxt = phone.getText().toString();
        String emailTxt = email.getText().toString();
        String passTxt = pass.getText().toString();
        String conf_passTxt = conf_pass.getText().toString();

        user = new User();

        // Check if user filled all the blanks.
        if (nameTxt.isEmpty() || surnameTxt.isEmpty() || phoneTxt.isEmpty() || emailTxt.isEmpty() || passTxt.isEmpty() || conf_passTxt.isEmpty()){
            Toast.makeText(this, "Please fill in the blanks!", Toast.LENGTH_SHORT).show();
        }

        // Check if passwords are matching with each other.
        else if (!(passTxt.equals(conf_passTxt))) {
            Toast.makeText(this, "Passwords are not matching!", Toast.LENGTH_SHORT).show();
        }

        else{
            mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                addDataToDatabase(nameTxt, surnameTxt, phoneTxt, emailTxt, passTxt);
                            }
                            else{
                                Toast.makeText(SignUp.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void addDataToDatabase(String Name, String Surname, String Phone, String Email, String Password){

        user.setName(Name);
        user.setSurname(Surname);
        user.setPhone(Phone);
        user.setEmail(Email);
        user.setPass(Password);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //reference.setValue(user);
                reference.child(mAuth.getUid()).setValue(user);
                Toast.makeText(SignUp.this, "User was created successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "Failed create user!. " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}