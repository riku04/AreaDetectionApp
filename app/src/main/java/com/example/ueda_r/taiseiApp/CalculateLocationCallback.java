package com.example.ueda_r.taiseiApp;

import android.location.Location;

public interface CalculateLocationCallback {
    public void onLocationCalculated(Location location);
    public void onStopGPS();
}
