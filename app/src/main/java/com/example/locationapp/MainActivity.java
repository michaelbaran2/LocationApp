package com.example.locationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;



public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {
    private static final String TAG = "LocationApp";
    private static final int REQUEST_LOCATION_PERMISSION = 3;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";

    private TextView mLastLocationTextView;
    private TextView mAddressTextView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mTrackingLocation;
    private Button button;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLastLocationTextView = findViewById(R.id.location_textview);
        mAddressTextView = findViewById(R.id.address_textview);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    startTrackingLocation();
                }
                else {
                    stopTrackingLocation();
                }
            }
        });
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation) {
                    Location location = locationResult.getLastLocation();
                    new FetchAddressTask(MainActivity.this, MainActivity.this).execute(location);
                    mLastLocationTextView.setText(getString(R.string.location_text, location.getLatitude(), location.getLongitude(), location.getTime()));
                }
            }
        };
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            Log.d(TAG, "Didn't have location permissions, but tried asking nicely");
        }
        else {
            Log.d(TAG, "Location permissions granted");
            mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
        }
        mTrackingLocation = true;
        button.setText(R.string.button_stop);
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            mTrackingLocation = false;
            button.setText(R.string.button_start);
            mLastLocationTextView.setText(R.string.default_text);
            mAddressTextView.setText("");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation) {
            mAddressTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));
            Log.d(TAG, "onTaskCompleted: Got address " + result);
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTrackingLocation) {
            startTrackingLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTrackingLocation) {
            stopTrackingLocation();
            // So the app will continue tracking when it resumes
            mTrackingLocation = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }
}