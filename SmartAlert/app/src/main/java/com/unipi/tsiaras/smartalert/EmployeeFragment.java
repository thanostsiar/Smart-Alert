package com.unipi.tsiaras.smartalert;

import static androidx.constraintlayout.widget.Constraints.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmployeeFragment extends Fragment {

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    RecyclerView alert_list;
    YourAdapter adapter;
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee, container, false);
        mAuth = FirebaseAuth.getInstance();
        alert_list = view.findViewById(R.id.alert_list);
        alert_list.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Alert> alerts = new ArrayList<>();
        adapter = new YourAdapter(alerts);
        alert_list.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("alerts");
        Log.d("Firebase", "Database reference: " + mDatabase.toString());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Alert> alerts = new ArrayList<>();

                for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                    Alert alert = alertSnapshot.getValue(Alert.class);
                    alerts.add(alert);
                    Log.d(TAG, "Alert: " + alert.getDisaster() + ", " + alert.getLatitude() + ", " + alert.getLongitude() + ", " + alert.getTimestamp());
                }
                Log.d(TAG, "Number of alerts: " + alerts.size());

                // Update the list of alerts in the adapter
                adapter.setAlerts(alerts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });


            // Inflate the layout for this fragment
        return view;
    }

    public class YourAdapter extends RecyclerView.Adapter<YourAdapter.ViewHolder> {
        private List<Alert> mAlerts;

        public YourAdapter(List<Alert> alerts) {
            mAlerts = alerts;
        }
        public void setAlerts(List<Alert> alerts) {
            mAlerts = alerts;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.alert_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Alert alert = mAlerts.get(position);
            holder.disasterTextView.setText(alert.getDisaster());
            holder.latitudeTextView.setText(alert.getLatitude());
            holder.longitudeTextView.setText(alert.getLongitude());
            holder.timestampTextView.setText(alert.getTimestamp());
            holder.commentTextView.setText(alert.getComments());
            String imageUrl = alert.getImg_url();

            Glide.with(getContext()).load(imageUrl).into(holder.alertimageview);
        }

        @Override
        public int getItemCount() {
            return mAlerts.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView disasterTextView,commentTextView,latitudeTextView,longitudeTextView,timestampTextView;
            public final ImageView alertimageview;
            Button btn_apply,btn_cancel;



            public ViewHolder(View view) {
                super(view);
                mView = view;
                disasterTextView = view.findViewById(R.id.alert_disaster);
                latitudeTextView = view.findViewById(R.id.alert_latitude);
                longitudeTextView = view.findViewById(R.id.alert_longitude);
                timestampTextView = view.findViewById(R.id.alert_ts);
                commentTextView = view.findViewById(R.id.alert_comment);
                alertimageview = view.findViewById(R.id.alert_image);
                btn_apply = view.findViewById(R.id.alert_item_btn_accept);
                btn_cancel = view.findViewById(R.id.alert_item_btn_decline);

                btn_apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


            }
        }




    }
}