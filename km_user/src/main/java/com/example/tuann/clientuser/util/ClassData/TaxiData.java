package com.example.tuann.clientuser.util.ClassData;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tuann on 3/23/16.
 */
public class TaxiData {
    private String latitude;
    private String longitude;
    @SerializedName("taxi_name") private String taxiName;
    @SerializedName("last_time_updated") private String lastTimeUpdated;
    @SerializedName("topics") private String topics;

    public String getTopics() {
        return topics;
    }

    public LatLng getLocation() {
        if(latitude != null && longitude != null
            && !latitude.isEmpty() && !longitude.isEmpty()) {
            LatLng location = new LatLng(Double.valueOf(latitude) , Double.valueOf(longitude));
            return location;
        }
        return null;
    };

    public String getTaxiName() {
        return taxiName;
    }

    public void setLocation (String lat , String lon) {
        latitude = lat;
        longitude = lon;
        taxiName = "test";
    }
}
