package com.unipi.tsiaras.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    TextView name, email;

    FirebaseAuth mAuth;

    Toolbar toolbar;

    NavigationView navigationView;

    FirebaseDatabase database;

    DatabaseReference usersRef;

    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();

        usersRef = database.getReference("users");

        navigationView = findViewById(R.id.nav_view);

        headerView = navigationView.getHeaderView(0);

        name = headerView.findViewById(R.id.navName);

        email = headerView.findViewById(R.id.navEmail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        if (mAuth.getCurrentUser() != null){
            String userId = mAuth.getUid();
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameDB = snapshot.child(userId).child("name").getValue(String.class);
                        String emailDB = snapshot.child(userId).child("email").getValue(String.class);
                        name.append(nameDB);
                        email.append(emailDB);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            /*usersRef.child(userId).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String imageUrl = snapshot.child()
                    }
                    //String imageUrl = snapshot.getValue(String.class);
                    Picasso.get().load(imageUrl).into(imageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/
            /*usersRef.child(userId).child("image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null) {
                        Picasso.get().load(imageUrl).into(imageView);
                        //imageView.setImageResource(R.drawable.person);
                        //storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            /*@Override
                            public void onSuccess(Uri uri) {
                                imageView.setImageResource(R.drawable.person);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Error", "onCancelled", error.toException());
                }
            });*/
        }

        //storageReference = storage.getReference().child("2131165429");

        /*storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (imageView != null) {
                    //Picasso.get().load(uri).into(imageView);
                    imageView.setImageResource(R.drawable.person);
                }
            }
        });*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_signout:
                if (mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    Intent intent = new Intent(this, SignIn.class);
                    startActivity(intent);
                    Toast.makeText(this, "Sign Out", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "No one is signed in.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    public void toProfile(View view){
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }
}