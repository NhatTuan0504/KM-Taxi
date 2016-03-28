package com.example.tuann.clientuser.util.View;

import android.location.Location;

import com.example.tuann.clientuser.util.ClassData.TaxiData;

import java.util.List;

/**
 * Created by tuann on 3/23/16.
 */
public interface IUserView {

    /**
     * Show TaxiLocations To Map
     * @param taxiDatas
     */
    public void showTaxiLocation(List<TaxiData> taxiDatas);

    /**
     *  Cancel Request , Send Message To View
     */
    public void stopShowMap() ;
}
