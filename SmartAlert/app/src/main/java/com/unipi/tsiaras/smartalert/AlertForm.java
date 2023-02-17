package com.unipi.tsiaras.smartalert;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;

public class AlertForm extends AppCompatActivity {
    EditText et;
    Button btn_img;
    Button btn_apply;
    Alert alert;
    LocationManager mLocationManager;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ActivityResultLauncher<String> mGetContent;
    final String[] options = {"Flood", "Fire", "Earthquake", "Hurricane"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_form);
        et = findViewById(R.id.alert_et);
        btn_img = findViewById(R.id.alert_btnimage);
        btn_apply = findViewById(R.id.alert_btn);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choose the natural disaster")
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedOption = options[which];
                        et.setText(selectedOption);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    // Do something with the selected image, such as displaying it in an ImageView
                    ImageView imageView = findViewById(R.id.alert_imageview);
                    imageView.setImageURI(result);
                }
            }
        });

        //Button to select image
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AlertForm.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(AlertForm.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AlertForm.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                } else {
                    // The required permissions are already granted
                    mGetContent.launch("image/*");
                }
            }
        });

        //Button to send alert
        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert = new Alert();
                if(!TextUtils.isEmpty(et.getText().toString())){
                    if (ContextCompat.checkSelfPermission(AlertForm.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(AlertForm.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED) {
                        Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String ts = timestamp.toString();
                        String latitude = loc.getLatitude()+"";
                        String longitude = loc.getLongitude()+"";
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        alert.setDisaster(et.getText().toString());
                        alert.setLatitude(latitude);
                        alert.setLongitude(longitude);
                        alert.setTimestamp(ts);
                        alert.setUid(uid);
                        //Add alert to database
                        reference.push().setValue(alert);
                        Toast.makeText(AlertForm.this, "Alert was sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(AlertForm.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
                    }
                }
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // The required permissions are granted
                mGetContent.launch("image/*");
            } else {
                // The required permissions are not granted so we ask them again
                ActivityCompat.requestPermissions(AlertForm.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }
}