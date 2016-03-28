package com.example.tuann.clientuser.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by truonghn on 3/15/16.
 */


public class NanoHTTP extends NanoHTTPD{

    public static final String TEXT_404 = "404 File Not Found";
    public Map<String, Response> urlToDataMap = new HashMap<String, Response>();

    private static final Logger LOG = Logger.getLogger(NanoHTTP.class.getName());

    public NanoHTTP(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        NanoHTTP.LOG.info(method + " '" + uri + "' ");

        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n" + "  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }

        msg += "</body></html>\n";

        return newFixedLengthResponse(msg);
    }

}
