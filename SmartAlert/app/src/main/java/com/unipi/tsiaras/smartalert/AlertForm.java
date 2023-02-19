package com.unipi.tsiaras.smartalert;

import static androidx.constraintlayout.widget.Constraints.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AlertForm extends AppCompatActivity {
    ImageView imageView;
    EditText et,et2;
    Button btn_img;
    Button btn_apply;
    Alert alert;
    LocationManager mLocationManager;
    Location loc;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    Query query;
    ProgressBar progressBar;
    Uri imageUri;
    int weight_final = 2;
    StorageReference storageReference;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ActivityResultLauncher<Intent> activityResultLauncher;
    final String[] options = {"Flood", "Fire", "Earthquake", "Hurricane"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_form);

        et = findViewById(R.id.alert_et);
        imageView = findViewById(R.id.alert_imageview);
        et2 = findViewById(R.id.alert_et2);
        btn_img = findViewById(R.id.alert_btnimage);
        btn_apply = findViewById(R.id.alert_btn);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("alerts");
        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        alert = new Alert();

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
                if (ContextCompat.checkSelfPermission(AlertForm.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(AlertForm.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    dialog.show();
                } else {
                    ActivityCompat.requestPermissions(AlertForm.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                }
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                } else {
                    Toast.makeText(AlertForm.this, "No Image Selected", Toast.LENGTH_SHORT).show();
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
                    Intent photoPicker = new Intent();
                    photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                    photoPicker.setType("image/*");
                    activityResultLauncher.launch(photoPicker);
                }
            }
        });


        //Button to send alert
        btn_apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getweight(new WeightListener() {
                    @Override
                    public void onWeightReceived(int weight) {
                        weight_final = weight;
                        System.out.println(weight_final);
                        apply_alert(loc, weight_final);
                    }
                });

            }
        });
    }

    public interface WeightListener  {
        void onWeightReceived(int weight);
    }

    public void getweight(WeightListener  listener){
        query = FirebaseDatabase.getInstance().getReference("alerts");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int weight_ = 2;
                for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                    Alert alert = alertSnapshot.getValue(Alert.class);
                    System.out.println(alert.getDisaster());
                    System.out.println(et.getText());
                    int lat = (int) Double.parseDouble(alert.getLatitude());
                    System.out.println(lat);
                    int lon = (int) Double.parseDouble(alert.getLongitude());
                    System.out.println(lon);
                    int lat_this = (int) Double.parseDouble(String.valueOf(loc.getLatitude()));
                    System.out.println(lat_this);
                    int lon_this = (int) Double.parseDouble(String.valueOf(loc.getLongitude()));
                    System.out.println(lon_this);
                    if (alert.getDisaster().equals(et.getText().toString()) && lat==lat_this && lon==lon_this) {
                        weight_ = 1;
                        alert.setWeight(1);
                        break;
                    }
                }
                query.removeEventListener(this);
                listener.onWeightReceived(weight_);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void apply_alert(Location loc,int weight){
        alert = new Alert();
        if(!TextUtils.isEmpty(et.getText().toString())){
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String ts = timestamp.toString();
                String latitude = loc.getLatitude()+"";
                String longitude = loc.getLongitude()+"";
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
            if (imageUri != null) {
                uploadToFirebase(imageUri, ts, latitude, longitude,weight);
            } else {
                alert.setDisaster(et.getText().toString());
                alert.setComments(et2.getText().toString());
                alert.setLatitude(latitude);
                alert.setLongitude(longitude);
                alert.setTimestamp(ts);
                alert.setUid(uid);
                alert.setWeight(weight);
                //Add alert to database
                databaseReference.push().setValue(alert);
                Toast.makeText(AlertForm.this, "Alert was sent!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // The required permissions are granted
                Intent photoPicker = new Intent();
                photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            } else {
                // The required permissions are not granted so we ask them again
                ActivityCompat.requestPermissions(AlertForm.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    private void uploadToFirebase(Uri uri, String timeStamp, String lat, String lon,int weight) {
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        alert.setDisaster(et.getText().toString());
                        alert.setComments(et2.getText().toString());
                        alert.setTimestamp(timeStamp);
                        alert.setLatitude(lat);
                        alert.setLongitude(lon);
                        alert.setUid(uid);
                        alert.setWeight(weight);
                        alert.setImg_url(uri.toString());

                        String key = databaseReference.push().getKey();
                        databaseReference.child(key).setValue(alert);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(AlertForm.this, "Created Alert!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(AlertForm.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
}
