package com.example.tuann.clientuser;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;

import com.example.tuann.clientuser.service.MqttService;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tuann on 3/22/16.
 */
public class MyApplication  extends Application{
    private String devideId ;
    private Location location;
    private static MyApplication application;

    public static  MyApplication getInstance(){
        if(application == null) {
            application = new MyApplication();
        }
        return application;
    }

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        MqttService.actionStart(getInstance());
    }

    public void setDevideId(Activity activity) {
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        devideId = tm.getDeviceId();
    }

    public String getDevideId() {
        return devideId;
    }
    public Location getLocation() {return  location;}
    public LatLng getLatLng() {
        if(location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return null;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
}
