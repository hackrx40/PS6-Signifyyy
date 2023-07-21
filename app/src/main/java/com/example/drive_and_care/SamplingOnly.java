package com.example.drive_and_care;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SamplingOnly extends AppCompatActivity {

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address,textView;
    Switch sw_locationupdates, sw_gps;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationRequest.Builder builder;
    long speed_score = 0;
    LocationCallback locationCallBack;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_lat = findViewById(R.id.latitude2);
        tv_lon = findViewById(R.id.longitude2);
//        tv_altitude = findViewById(R.id.);
        tv_accuracy = findViewById(R.id.accuracy2);
        tv_speed = findViewById(R.id.speed2);
        tv_address = findViewById(R.id.address2);
        sw_gps = findViewById(R.id.switch1);
        textView = findViewById(R.id.textView);

        builder = new LocationRequest.Builder(30000)
                .setMinUpdateIntervalMillis(5000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        locationRequest = builder.build();

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                updateUIValues(location);

            }
        };

        sw_gps.setOnClickListener(view -> {
            if (sw_gps.isChecked()) {
                builder = new LocationRequest.Builder(30000)
                        .setMinUpdateIntervalMillis(5000)
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                locationRequest = builder.build();
                tv_sensor.setText("Using GPS sensors");
            } else {
                builder = new LocationRequest.Builder(30000)
                        .setMinUpdateIntervalMillis(5000)
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest = builder.build();
                tv_sensor.setText("Using Towers + WIFI");
            }
        });

        sw_locationupdates.setOnClickListener(view -> {
            if (sw_locationupdates.isChecked()) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });
        updateGPS();
    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 99:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else
                {
                    Toast.makeText(this, "This app requires.",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                }
            });
        }
        else
        {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},99);
        }
    }

    private void updateUIValues(Location location){

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else
        {
            tv_altitude.setText("Not available");
        }
        if(location.hasSpeed()){

            tv_speed.setText(String.valueOf(location.getSpeed()));
            if(location.getSpeed()<20.0)
            {
                speed_score++;
            }
            else if(location.getSpeed() > 20.0)
            {
                speed_score += 4;
            }
            else if(location.getSpeed() > 40 && location.getSpeed() < 60)
            {
                speed_score += 3;
            }
            else
            {
                speed_score += 2;
            }
            textView.setText(Long.toString(speed_score));
        }
        else
        {
            tv_speed.setText("Not available");
        }
        Geocoder geocoder = new Geocoder(this);


        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }catch (Exception e)
        {
            tv_address.setText("Unable to get street address");
        }
    }

}