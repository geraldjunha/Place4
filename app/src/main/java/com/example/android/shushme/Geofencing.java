package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OCHA on 07/09/2017.
 */

public class Geofencing implements ResultCallback {
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        // Check that the API client is connected and that the list has Geofences in it
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in registerGeofences
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence : %s",
                result.getStatus().toString()));
    }
}
