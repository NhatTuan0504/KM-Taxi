package com.example.tuann.clientuser.util.Model;

import android.app.Activity;
import android.location.Location;

import com.example.tuann.clientuser.util.ClassData.TaxiData;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by tuann on 3/23/16.
 */
public interface IUserModel {

    public int a = 0 ;
    public int b = 10 ;
    public int c = 100;

    /**
     * Get Current User Location
     * @param activity
     * @return Location
     */
    public Location getUserLocation(Activity activity) ;

    /**
     * Get List Taxi Location form Server
     * @return
     */
    public List<TaxiData> getListLocationOfTaxi();

    public void setListLocationOfTaxi(String object) ;
}
