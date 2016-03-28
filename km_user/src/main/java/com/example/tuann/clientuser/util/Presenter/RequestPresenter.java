package com.example.tuann.clientuser.util.Presenter;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;

import com.example.tuann.clientuser.MyApplication;
import com.example.tuann.clientuser.service.UserService;
import com.example.tuann.clientuser.util.Model.Receiver.UserReceiver;
import com.example.tuann.clientuser.util.View.IUserView;

/**
 * Created by tuann on 3/22/16.
 */
public class RequestPresenter {
    public static String HTTP_REQUEST = "http_request";
    public final static int GET_HTTP_REQUEST = 1;
    public final static int POST_HTTP_REQUEST = 2;
    public final static int POST_HTTP_UPDATE = 3;
    public final static int POST_HTTP_CANCEL = 4;

    public final static int TIME_REPEAT_UPDATE = 10000;

    //PARAM
    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";
    public static String DEVICE_ID = "device_id";

    private  IUserView userView ;//Use to show Data to View
    private UserReceiver userReceiver;    // Get Data for User
    private static boolean flagRequest = false;   // Flag to check Server is called Requested or NOT
    private static Activity activity;            // activity Parent to call method of activity

    public static boolean getFlagRequest() {
        return flagRequest;
    }

    public RequestPresenter(Activity activity , IUserView view) {
        this.activity = activity;
        userReceiver = new UserReceiver(this);
        userView = view;

        //Regist BroadCast Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UserService.RESULT_RECEIVER);
        activity.registerReceiver(userReceiver, intentFilter);
    }

    /**
     * Get Location of User
     */
    private void getLocation() {
        if(activity != null) {
           Location location = userReceiver.getUserLocation(activity);
            MyApplication.getInstance().setLocation(location);
        }
    }

    /**
     * Call Server Api to Handle Requests form User
     * @param request int
     */
    public void callServerApi(final int request) {
        //Check Request
        switch (request) {
            case POST_HTTP_REQUEST:
                //in Case Had call Request don't call action
                if(flagRequest) {
                   return;
                }
                //get Location of User
                getLocation();
                flagRequest = true;
                break;
            case POST_HTTP_UPDATE:
                //Check Request is Call or Not
                if(!flagRequest) {
                    return;
                }
                break;
            case POST_HTTP_CANCEL:
                //Cancel Call Update location
                flagRequest = false;
                break;
            default:
                break;
        }

        //Call Service
        callService(request);
    }

    /**
     * Call Service to run Api
     * @param request
     */
    public static void callService(int request) {
        Intent intent = new Intent(activity, UserService.class);
        intent.putExtra(HTTP_REQUEST, request);
        activity.startService(intent);
    }

    /**
     * Call Back to View To Show Data
     * @param type
     */
    public void callBack(int type){

        switch (type) {
            case POST_HTTP_REQUEST:
                break;
            case POST_HTTP_UPDATE:
                userView.showTaxiLocation(userReceiver.getListLocationOfTaxi());
                break;
            case POST_HTTP_CANCEL:
                userView.stopShowMap();
            default:
                break;
        }
    }

    public void stopService() {
        try {
            Intent intent = new Intent(activity, UserService.class);
            activity.stopService(intent);
            if (userReceiver != null) {
                activity.unregisterReceiver(userReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Resources getResouces() {
        return activity.getResources();
    }

    /**
     * Test Array Data
     */
//    private void testArray() {
//        InputStream XmlFileInputStream = activity.getResources().openRawResource(R.raw.gpx_example);
//        String s = readTextFile(XmlFileInputStream);
//
//        Document document = Jsoup.parse(s);
//
//        Elements elements = document.getElementsByTag("trkpt");
//
//        for (Element e : elements) {
//            Attributes attributes =  e.attributes();
//            TaxiData data = new TaxiData();
//            data.setLocation(attributes.get("lat") , attributes.get("lon"));
//            listLocation.add(data);
//            if(listLocation.size() > 200) {
//                break;
//            }
//        }
//    }
//    public String readTextFile(InputStream inputStream) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        byte buf[] = new byte[1024];
//        int len;
//        try {
//            while ((len = inputStream.read(buf)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.close();
//            inputStream.close();
//        } catch (IOException e) {
//
//        }
//        return outputStream.toString();
//    }
}
