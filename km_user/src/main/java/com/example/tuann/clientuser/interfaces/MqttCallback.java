package com.example.tuann.clientuser.interfaces;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tuann.clientuser.MyApplication;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by truonghn on 3/17/16.
 */
public class MqttCallback implements org.eclipse.paho.client.mqttv3.MqttCallback {
    public static final String MARKER_EVENT = "marker_event";
    public static final String BROKWR_CONTENT_SEND = "broker_content";

    /**
     * connect fail
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {

    }

    /**
     * get a sms from server
     * @param topic
     * @param mqttMessage
     * @throws Exception
     */

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.e(getClass().getName(), "  Topic:\t" + topic +
                "  Message:\t" + new String(mqttMessage.getPayload()) +
                "  QoS:\t" + mqttMessage.getQos());

//        Intent intent = new Intent(MARKER_EVENT);
//        intent.putExtra(BROKWR_CONTENT_SEND, new String(mqttMessage.getPayload()));
//        LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    /**
     * register a local broatcast
     */

}
