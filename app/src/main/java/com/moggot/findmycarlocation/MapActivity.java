package com.moggot.findmycarlocation;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MapActivity extends NetworkActivity implements OnMapReadyCallback, NetworkActivity.LocationObserver {

    final static String LOG_TAG = "MapActivity";
    private BroadcastReceiver receiver;

    private Map mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.screen_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Ad advertisment = new Ad(this);
        advertisment.showBanner(R.id.adViewMap);
        advertisment.showInterstitial(R.string.banner_ad_unit_id_map_interstitial);

        FirebaseAnalysis firebase = new FirebaseAnalysis(this);
        firebase.init();

        registerLocationObserver(this);
        initLocationServices();

        IntentFilter filter = new IntentFilter(Consts.PACKAGE_NAME);
        receiver = new ProximityIntentReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = new Map(this, map);
        checkLocationSettings();
    }

    @Override
    public void onScanLocationStarted(final NetworkActivity activity) {
        Log.i(LOG_TAG, "MaponScanLocationStarted");
    }

    @Override
    public void onScanLocationFinished(final NetworkActivity activity) {
        Location location = getLocation();
        if (location == null)
            return;
        mMap.setUpMap(location);
        Log.i(LOG_TAG, "MaponScanLocationFinished = " + location);
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop");
        super.onStop();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();
        unregisterLocationObserver(this);
    }

}