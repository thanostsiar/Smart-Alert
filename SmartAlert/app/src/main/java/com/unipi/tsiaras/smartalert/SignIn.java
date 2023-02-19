package com.unipi.tsiaras.smartalert;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Config;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class SignIn extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseAuth mAuth;
    EditText email, pass;
    Button btn_sign_up;
    Button btn_sign_in;
    FirebaseDatabase database;
    DatabaseReference reference;
    String[] countryNames={"English","Ελληνικά"};
    int[] flags = {R.drawable.uk_flag, R.drawable.gre_flag};
    boolean first_trigger = true;
    int langpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        email = findViewById(R.id.et_email);
        pass = findViewById(R.id.et_password);
        btn_sign_up = findViewById(R.id.button_signup);
        btn_sign_in = findViewById(R.id.button_signin);
        Spinner spin = (Spinner) findViewById(R.id.spinner_);
        spin.setOnItemSelectedListener(this);
        CustomAdapter customAdapter=new CustomAdapter(getApplicationContext(),flags,countryNames);
        spin.setAdapter(customAdapter);

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


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String selectedlang = adapterView.getItemAtPosition(position).toString();
        if(first_trigger){
            first_trigger = false;
        }else{
            if(selectedlang.equals("English")){
                langpos = 0;
            }
            else if(selectedlang.equals("Ελληνικά")){
                langpos = 1;
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void changelang(View view){
        switch (langpos){
            case 0:
                updateconfig("en");
                break;
            case 1:
                updateconfig("el");
                break;
            default:
                break;
        }
    }


    @SuppressWarnings("deprecation")
    public void updateconfig(String s){
        Locale locale=new Locale(s);
        Locale.setDefault(locale);
        Configuration configuration=new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration,
                getBaseContext().getResources().getDisplayMetrics());
        Bundle bundle=new Bundle();
        onCreate(bundle);
        setTitle(R.string.app_name);
    }
}