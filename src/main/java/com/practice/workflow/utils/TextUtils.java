package com.practice.workflow.utils;

import org.json.JSONObject;

public class TextUtils {


    public static String preparePayloadString(String adTitle, String adDescription){

        JSONObject payload = new JSONObject();
        payload.put("q",new String[]{adTitle,adDescription});
        payload.put("source","ar");
        payload.put("target","en");
        payload.put("format","text");
        return payload.toString();
    }
}
