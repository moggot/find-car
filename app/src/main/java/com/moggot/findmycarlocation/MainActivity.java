package com.moggot.findmycarlocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    float y1, y2;
    ImageView img_animation;
    int height;

    int trigger = 0;
    final static String LOG_TAG = "myLogs";
    boolean isLocationSaved;

    public boolean isAnimation = false;
    private boolean repeat = true;
    private boolean show_map = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        Log.d(LOG_TAG, "start");
        ((AnalyticsApplication) getApplication())
                .getTracker(AnalyticsApplication.TrackerName.APP_TRACKER);

        isLocationSaved = SharedPreference.LoadIsLocationSavedState(this);
        img_animation = (ImageView) findViewById(R.id.ivTrigger);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
    }

    public boolean onTouchEvent(MotionEvent touchevent) {
        isLocationSaved = SharedPreference.LoadIsLocationSavedState(this);
        if (isAnimation)
            return false;
        switch (touchevent.getAction()) {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN: {
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                y2 = touchevent.getY();

                if (y1 < y2) {
                    Log.d(LOG_TAG, "isLocationSaved = " + isLocationSaved);
                    Log.d(LOG_TAG, "trigger = " + trigger);
                    isLocationSaved = SharedPreference
                            .LoadIsLocationSavedState(this);
                    // if UP to DOWN sweep event on screen
                    if (trigger == 0) {
                        if (!isLocationSaved) {
                            save_car_location();
                            break;
                        }
                        show_map = true;
                        animationDown(0, height / 9);
                        trigger = -1;
                        break;
                    }
                }

                // if Down to UP sweep event on screen
                if (y1 > y2) {

                    if (trigger == -1) {
                        animationDown(height / 9, 0);
                        trigger = 0;
                        break;
                    }
                    if (trigger == 0) {
                        if (isLocationSaved) {
                            find_your_car();
                            break;
                        }
                        animationUP();
                        Calendar time = Calendar.getInstance();
                        int cur_day = time.get(Calendar.DAY_OF_MONTH);
                        int cur_hour = time.get(Calendar.HOUR_OF_DAY);
                        int cur_minute = time.get(Calendar.MINUTE);
                        SharedPreference.SaveTime(this, cur_day, cur_hour,
                                cur_minute);
                        SharedPreference.SaveIsLocationSavedState(this, true);
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String provider = locationManager.getBestProvider(criteria,
                                false);
                        if (provider != null && !provider.equals("")) {
                            Location location = null;
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                location = locationManager
                                        .getLastKnownLocation(provider);
                            }

                            if (location != null) {

                                Log.d(LOG_TAG,
                                        "location_lng = " + location.getLongitude());
                                Log.d(LOG_TAG,
                                        "location_lat = " + location.getLatitude());

                                // SharedPreference.SaveLocation(this, 55.928,
                                // 37.520);
                                SharedPreference.SaveLocation(this,
                                        location.getLatitude(),
                                        location.getLongitude());
                            } else {
                                no_location();
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        } else {
                            no_provider();
                            startActivity(new Intent(
                                    android.provider.Settings.ACTION_WIFI_SETTINGS));
                        }

                        trigger = 0;
                        break;
                    }
                }
            }
            break;

        }
        return false;
    }


    void animationUP() {
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f,
                0, -height / 9);

        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (repeat) {
                    TranslateAnimation animation_repeate = new TranslateAnimation(0.0f, 0.0f,
                            -height / 9, 0);
                    animation_repeate.setDuration(1000);
                    animation_repeate.setFillAfter(true);
                    img_animation.startAnimation(animation_repeate);
                    car_loc_save_success();
                    repeat = false;
                    isAnimation = false;
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setDuration(1000);
        animation.setFillAfter(true);
        img_animation.startAnimation(animation);
    }

    void animationDown(float start, float end) {
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f,
                start, end);

        isLocationSaved = SharedPreference.LoadIsLocationSavedState(this);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isLocationSaved && show_map) {
                    Intent intent = new Intent(MainActivity.this,
                            ScreenMap.class);
                    startActivityForResult(intent, 1);
                    isAnimation = false;
                }
                isAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setDuration(1000);
        animation.setFillAfter(true);
        img_animation.startAnimation(animation);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return true;
        }
        if (id == R.id.info) {
            Intent intent = new Intent(MainActivity.this, ScreenInfo.class);
            startActivityForResult(intent, 2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            animationDown(height / 9, 0);
            trigger = 0;
            repeat = true;
            show_map = false;
        }
    }

    private void no_location() {
        Toast.makeText(getBaseContext(), R.string.no_location,
                Toast.LENGTH_SHORT).show();
    }

    private void no_provider() {
        Toast.makeText(getBaseContext(), R.string.no_provider,
                Toast.LENGTH_SHORT).show();
    }

    private void find_your_car() {
        Toast.makeText(getBaseContext(), R.string.find_your_car,
                Toast.LENGTH_SHORT).show();
    }

    private void save_car_location() {
        Toast.makeText(getBaseContext(), R.string.save_car_location,
                Toast.LENGTH_SHORT).show();
    }

    private void car_loc_save_success() {
        Toast.makeText(getBaseContext(), R.string.save_car_loc,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get an Analytics tracker to report app starts & uncaught exceptions
        // etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);

    }

    @Override
    protected void onStop() {

        // Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }
}