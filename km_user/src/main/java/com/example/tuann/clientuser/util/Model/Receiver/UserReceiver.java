package com.example.tuann.clientuser.util.Model.Receiver;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.example.tuann.clientuser.MyApplication;
import com.example.tuann.clientuser.R;
import com.example.tuann.clientuser.service.MqttService;
import com.example.tuann.clientuser.service.UserService;
import com.example.tuann.clientuser.util.ClassData.TaxiData;
import com.example.tuann.clientuser.util.Model.IUserModel;
import com.example.tuann.clientuser.util.Presenter.RequestPresenter;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Created by tuann on 3/23/16.
 */
public class UserReceiver extends BroadcastReceiver implements IUserModel {

    List<TaxiData>  locationOfTaxi = new ArrayList<>();
    RequestPresenter requestPresenter;
    private MemoryPersistence mMemStore; 		// Save to MemoryStore
    private MqttClient mClient;
    private List<String> listDeviceID  = new ArrayList<>();

    public UserReceiver(RequestPresenter presenter) {
        this.requestPresenter = presenter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //getData From Service
        String data = intent.getStringExtra(UserService.RESULT_TAXI_DATA);
        int type = intent.getIntExtra(UserService.RESULT_TYPE, 0);

        if(type == RequestPresenter.POST_HTTP_REQUEST
            || type == RequestPresenter.POST_HTTP_UPDATE ) {
            setListLocationOfTaxi(data);
        }

        //Push Message to Server
        if(type == RequestPresenter.POST_HTTP_REQUEST) {
            listDeviceID.clear();
            LatLng location = MyApplication.getInstance().getLatLng();
            String s = requestPresenter.getResouces().getString(R.string.request_taxi, location.latitude , location.longitude );
            for(TaxiData taxi : locationOfTaxi) {
                if(taxi.getTopics() == null || taxi.getTopics().isEmpty()) {
                    continue;
                }
                listDeviceID.add(taxi.getTopics());
            }
            MqttService.publishMessage(s , listDeviceID);
        } else if (type == RequestPresenter.POST_HTTP_CANCEL) {
            LatLng location = MyApplication.getInstance().getLatLng();
            String s = requestPresenter.getResouces().getString(R.string.cancel_request_taxi, location.latitude , location.longitude );
            MqttService.publishMessage(s , listDeviceID);
        }

        //callBack To Presenter
        requestPresenter.callBack(type);
    }


    @Override
    public List<TaxiData> getListLocationOfTaxi() {
        return locationOfTaxi;
    }

    @Override
    public void setListLocationOfTaxi(String data) {
        List<TaxiData> locations = new ArrayList<>();
        //set Data To location Of Taxi
        if(data != null && !data.isEmpty()) {
            Gson gson = new Gson();
            Type typeData = new TypeToken<List<TaxiData>>(){}.getType();
            try {
                locations = gson.fromJson(data, typeData);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(!locations.isEmpty()) {
            locationOfTaxi.clear();
            locationOfTaxi.addAll(locations);
        }
    }

    @Override
    public Location getUserLocation(Activity activity) {
        // Get the location manager
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        //check Permission to get Location
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, activity.getIntent(),PendingIntent.FLAG_UPDATE_CURRENT);
        //Request Update Location
        locationManager.requestSingleUpdate(provider , pendingIntent);

        //get current Location
        Location location = locationManager.getLastKnownLocation(provider);
        return location;
    }
}
