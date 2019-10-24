package com.example.ueda_r.taiseiApp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class CalculateLocation implements LocationListener {

    private CalculateLocationCallback callback;

    private LocationManager locationManager;
    private Context context;

    private int runTime = 10;
    private long MinTime = 1;
    private float MinDistance = 0;

    private Boolean isGpsStarted = false;

    CalculateLocation(Context context, int runTimeSec, long MinTime, float MinDistance, CalculateLocationCallback callback) {
        this.context = context;
        this.runTime = runTimeSec;
        this.MinTime = MinTime;
        this.MinDistance = MinDistance;
        this.callback = callback;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("CalcLocation", "location changed");
        this.callback.onLocationCalculated(location);
    }

    protected void startGPS() {
        if (!isGpsStarted) {
            isGpsStarted = true;
            Log.i("CalcLocation", "startGPS");

            final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                enableLocationSettings();
            }

            if (locationManager != null) {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MinTime, MinDistance, this);

                    Log.i("CalcLocation", "location update requested");
                    Log.i("CalcLocation", "MinTime = " + Long.toString(MinTime) + ", MinDistance = " + Float.toString(MinDistance));

                } catch (Exception e) {
                    Log.i("CalcLocation", e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.i("CalcLocation", "LocationManager is null");
            }
        } else {
            Log.i("CalcLocation", "GPS is already started");
        }
    }

    protected void stopGPS() {
        if (isGpsStarted) {
            isGpsStarted = false;
            Log.i("CalcLocation", "stopGPS");

            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(this);
            }
        } else {
            Log.i("CalcLocation", "GPS is already stopped");
        }
        this.callback.onStopGPS();
    }

    private void enableLocationSettings() {
        Log.i("CalcLocation", "enableLocationSettings");
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(settingsIntent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        Log.i("CalcLocation","onStatusChanged");
        switch (status){
            case LocationProvider.AVAILABLE:
                Log.i("CalcLocation", "=> AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.i("CalcLocation", "=> OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i("CalcLocation", "=> UNAVAILABLE");
                break;
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }


}
