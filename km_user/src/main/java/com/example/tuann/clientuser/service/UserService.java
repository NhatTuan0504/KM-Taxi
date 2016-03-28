package com.example.tuann.clientuser.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.tuann.clientuser.MyApplication;
import com.example.tuann.clientuser.util.Model.Receiver.UserReceiver;
import com.example.tuann.clientuser.util.Presenter.RequestPresenter;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserService extends Service {

    private final String URL_REQUEST_TAXI = "http://104.155.230.91/taxis/user_request.php";
    private final String URL_UPDATE_TAXI = "http://104.155.230.91/taxis/user_update_taxi_location.php";
    private final String URL_CANCEL_TAXI = "http://104.155.230.91/taxis/user_cancel_request.php";

//    private final String URL_REQUEST_TAXI = "http://10.10.21.21/Taxis/user_request.php";
//    private final String URL_UPDATE_TAXI = "http://10.10.21.21/Taxis/user_update_taxi_location.php";
//    private final String URL_CANCEL_TAXI = "http://10.10.21.21/Taxis/user_cancel_request.php";

    public final static int TIME_OUT = 20000;
    public final static int TIME_STOP_SERVICE = 600000;

    public final static String RESULT_RECEIVER = "result_receiver";
    public final static String RESULT_TAXI_DATA = "result_taxidata";
    public final static String RESULT_TYPE = "result_type";

    private UserReceiver userReceiver;

    public UserService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        int type = intent.getIntExtra(RequestPresenter.HTTP_REQUEST, 0);

        switch (type) {
            case RequestPresenter.POST_HTTP_REQUEST:
                postRequestTaxi();
                break;
            case RequestPresenter.POST_HTTP_UPDATE:
                postUpdateTaxi();
                break;
            case RequestPresenter.POST_HTTP_CANCEL:
                postCancelTaxi();
                break;
            default:
                break;
        }

        return START_REDELIVER_INTENT;
    }


    /**
     * call this method if you want to request data from server
     */
    public void postRequestTaxi() {
        final List<String> listDeviceId = new ArrayList<String>();
        final String devideID = MyApplication.getInstance().getDevideId();
        Location location = MyApplication.getInstance().getLocation();

        if (devideID != null && !devideID.isEmpty() && location != null) {
            Ion.with(getApplicationContext())
                .load(URL_REQUEST_TAXI)
                .setTimeout(TIME_OUT)
                .setBodyParameter(RequestPresenter.DEVICE_ID, devideID)
                .setBodyParameter(RequestPresenter.LATITUDE, String.valueOf(location.getLatitude()))
                .setBodyParameter(RequestPresenter.LONGITUDE, String.valueOf(location.getLongitude()))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(result != null) {
                            Log.e("TUAN", result.getResult());
                            callBackData(result.getResult(), RequestPresenter.POST_HTTP_REQUEST);
                        }
                    }
                });
        }
    }

    public void postUpdateTaxi() {

        String devideID = MyApplication.getInstance().getDevideId();

        if (devideID != null && !devideID.isEmpty() ) {
            Ion.with(getApplicationContext())
                .load(URL_UPDATE_TAXI)
                .setTimeout(TIME_OUT)
                .setBodyParameter(RequestPresenter.DEVICE_ID, devideID)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (result != null) {
                            try {
                                Log.e("TUAN", result.getResult());
                                callBackData(result.getResult(), RequestPresenter.POST_HTTP_UPDATE);

                                //Create timer
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (RequestPresenter.getFlagRequest()) {
                                            postUpdateTaxi();
                                        }
                                    }
                                }, RequestPresenter.TIME_REPEAT_UPDATE);
                            } catch (Exception ex) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        }
    }

    public void postCancelTaxi() {

        String devideID = MyApplication.getInstance().getDevideId();
        Location location = MyApplication.getInstance().getLocation();

        if (devideID != null && !devideID.isEmpty() && location != null) {
            Log.e("TUAN", "Push Message CANCEL");
            //Push Message stop Connect
            callBackData("", RequestPresenter.POST_HTTP_CANCEL);
//            Ion.with(getApplicationContext())
//                    .load(URL_CANCEL_TAXI)
//                    .setTimeout(TIME_OUT)
//                    .setBodyParameter(RequestPresenter.DEVICE_ID, devideID)
//                    .setBodyParameter(RequestPresenter.LATITUDE, String.valueOf(location.getLatitude()))
//                    .setBodyParameter(RequestPresenter.LONGITUDE, String.valueOf(location.getLongitude()))
//                    .asString()
//                    .withResponse()
//                    .setCallback(new FutureCallback<Response<String>>() {
//                        @Override
//                        public void onCompleted(Exception e, Response<String> result) {
//                            if(result != null) {
//                                callBackData(result.getResult(), RequestPresenter.POST_HTTP_CANCEL);
//                            }
//                        }
//                    });
        }
    }

    /**
     * Call Back Data , Send Data to BroadCast (UserReceiver)
     * @param result
     * @param type
     */
    public void callBackData(String result , int type) {

        //Start Intent , Send Data to BroadCast
        Intent intent = new Intent();
        intent.setAction(RESULT_RECEIVER);

        //In Case Type is
        switch (type) {
            case RequestPresenter.POST_HTTP_REQUEST:
            case RequestPresenter.POST_HTTP_UPDATE:
            case RequestPresenter.POST_HTTP_CANCEL:
                intent.putExtra(RESULT_TAXI_DATA , result);
                intent.putExtra(RESULT_TYPE , type);
                break;
            default:
                break;
        }

        //Send BroadCast
        sendBroadcast(intent);
    }
}
