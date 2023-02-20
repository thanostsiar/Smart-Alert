package com.unipi.tsiaras.smartalert;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    TextView name, surname, email, phone;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    String[] countryNames={"English","Ελληνικά"};

    int[] flags = {R.drawable.uk_flag, R.drawable.gre_flag};

    boolean first_trigger = true;

    Button btn;

    int langpos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        name = view.findViewById(R.id.profile_name);
        surname = view.findViewById(R.id.profile_surname);
        email = view.findViewById(R.id.profile_email);
        phone = view.findViewById(R.id.profile_phone);
        Spinner spin = (Spinner) view.findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        CustomAdapter customAdapter=new CustomAdapter(getContext(),flags,countryNames);
        spin.setAdapter(customAdapter);
        btn = view.findViewById(R.id.apply_profile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (langpos){
                    case 0:
                        updateconfig("en");
                        requireActivity().recreate();
                        break;
                    case 1:
                        updateconfig("el");
                        requireActivity().recreate();
                        break;
                    default:
                        break;
                }
            }
        });

        if (mAuth.getCurrentUser() != null){
            String userId = mAuth.getUid();
            String emailTxt = email.getText().toString();
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameDB;
                        String surnameDB;
                        String emailDB;
                        String phoneDB;

                        if(mAuth.getCurrentUser().getEmail().contains("@gov.gr")){
                            nameDB = "";
                            surnameDB = "";
                            emailDB = snapshot.child("civil protection").child(userId).child("email").getValue(String.class);
                            phoneDB = "";

                            email.append(emailDB);
                        }
                        else{
                            nameDB = snapshot.child("users").child(userId).child("name").getValue(String.class);
                            surnameDB = snapshot.child("users").child(userId).child("surname").getValue(String.class);
                            emailDB = snapshot.child("users").child(userId).child("email").getValue(String.class);
                            phoneDB = snapshot.child("users").child(userId).child("phone").getValue(String.class);

                            name.append(nameDB);
                            surname.append(surnameDB);
                            email.append(emailDB);
                            phone.append(phoneDB);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

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
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @SuppressWarnings("deprecation")
    private void updateconfig(String s){
        Locale locale=new Locale(s);
        Locale.setDefault(locale);
        Configuration configuration=new Configuration();
        configuration.setLocale(locale);
        requireContext().getResources().updateConfiguration(configuration,
                requireContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", s);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        updateconfig(language);
    }
}