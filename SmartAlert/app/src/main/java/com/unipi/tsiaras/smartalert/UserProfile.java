package com.unipi.tsiaras.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

public class UserProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] countryNames={"English","Ελληνικά"};
    int[] flags = {R.drawable.uk_flag, R.drawable.gre_flag};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        CustomAdapter customAdapter=new CustomAdapter(getApplicationContext(),flags,countryNames);
        spin.setAdapter(customAdapter);
    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        String selectedlang = arg0.getItemAtPosition(position).toString();
        if(selectedlang.equals("English")){
            setLocal(this,"en");
            finish();
            startActivity(getIntent());
        }
        else if(selectedlang.equals("Ελληνικά")){
            setLocal(this,"el");
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void setLocal(Activity activity, String langcode){
        Locale locale = new Locale(langcode);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config,resources.getDisplayMetrics());
    }
}