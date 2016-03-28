package com.example.tuann.clientuser;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.tuann.clientuser.service.MqttService;
import com.example.tuann.clientuser.util.ClassData.TaxiData;
import com.example.tuann.clientuser.util.LatLngInterpolator;
import com.example.tuann.clientuser.util.MarkerAnimation;
import com.example.tuann.clientuser.util.Presenter.RequestPresenter;
import com.example.tuann.clientuser.util.View.IUserView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener, IUserView {

    private static final int SHAKE_THRESHOLD = 14;
    private static final float ZOOM_RATE = 16f;

    private GoogleMap mMap;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private ImageView imageView;

    private long lastUpdate = 0;
    private long timeNow = 0;
    private float last;

    private HashMap<String , Marker>  listMarkers = new HashMap<>();

    private RequestPresenter requestPresenter;

    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        imageView = (ImageView) findViewById(R.id.ivLogo);
        imageView.setBackgroundResource(R.drawable.flash_logo);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                updateLocationTaxi();
                return true;
            }
        });

        requestPresenter = new RequestPresenter(this, this);
        MyApplication.getInstance().setDevideId(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Start Connection
        MqttService.actionStart(this);
        //Request Start Location Provider
        RequestUpdateLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                cancelRequestTaxi();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float current = event.values[0] + event.values[1] + event.values[2];
        timeNow = System.currentTimeMillis();
        long timeDiff = 0;

        if (lastUpdate == 0) {
            lastUpdate = timeNow;
        } else {
            timeDiff = timeNow - lastUpdate;

            if (timeDiff > 0) {
                float force = Math.abs(current - last);

                if (Float.compare(force, SHAKE_THRESHOLD) > 0) {
                    requestTaxi();
                }
                last = current;
                lastUpdate = timeNow;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void requestTaxi() {
        //start Flash
        AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
        frameAnimation.start();

        //Call Api , Request Taxi
        requestPresenter.callServerApi(RequestPresenter.POST_HTTP_REQUEST);
    }

    public void updateLocationTaxi() {
        //call Api Update Location of Taxi
        requestPresenter.callServerApi(RequestPresenter.POST_HTTP_UPDATE);
    }

    public void cancelRequestTaxi() {
        //Call Api , Finished Request Taxi
        requestPresenter.callServerApi(RequestPresenter.POST_HTTP_CANCEL);
    }

    @Override
    public void showTaxiLocation(List<TaxiData> taxiDatas) {

        //check ShowMap
        imageView.setVisibility(View.GONE);

        //clear listMarker
        listMarkers.clear();

        //Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // turn on My location
        mMap.setMyLocationEnabled(true);
        LatLng myLocation = MyApplication.getInstance().getLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, ZOOM_RATE));

        MarkerAnimation animation = new MarkerAnimation();
        LatLngInterpolator.Spherical interporlator = new LatLngInterpolator.Spherical();

        for (TaxiData data : taxiDatas) {
            String name = data.getTaxiName();
            Marker marker = null;
            if(listMarkers.get(name) != null) {
                marker =  listMarkers.get(name);
                //make animation for marker
                animation.animateMarkerToGB(marker , data.getLocation() , interporlator);
            } else {
                try {
                    LatLng coordinates = data.getLocation();
                    MarkerOptions markerOptions = new MarkerOptions().position(coordinates).title(data.getTaxiName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    marker = mMap.addMarker(markerOptions);
                    listMarkers.put(data.getTaxiName(), marker);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void stopShowMap() {
        listMarkers.clear();
        imageView.setVisibility(View.VISIBLE);
        AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
        frameAnimation.stop();

    }

    private void RequestUpdateLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        //Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if(provider != null && locationManager.isProviderEnabled(provider)) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, getIntent(),PendingIntent.FLAG_UPDATE_CURRENT);
            //Request Update Location
            locationManager.requestSingleUpdate(provider , pendingIntent);
        } else {
            //Request Turn on Location Service
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Please Turn on Your Location Service");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Request Turn On Location
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if(requestPresenter != null) {
            requestPresenter.stopService();
        }

        super.onDestroy();
    }
}
