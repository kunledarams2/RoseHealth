package com.tremendoc.tremendocdoctor.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public enum Type { SENT, INCOMING }

    private Type type;
    private String content;
    private String sender;

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }


    public static Message parse(String json) {
        Message message = new Message();
        try {
            JSONObject obj = new JSONObject(json);
            message.setContent(obj.getString("message"));
            message.setSender("");
            message.setType(Type.INCOMING);
        } catch (JSONException e){
        }
        return message;
    }



    public static Message dirtyParse(String json) {
        Message message = new Message();
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject msg = new JSONObject(obj.getString("message"));
            message.setContent(msg.getString("message"));
            message.setSender(msg.getString("sender"));
            message.setType(Type.INCOMING);
        } catch (JSONException e){
            Log.d("Message.dirtyParse  ", e.getMessage());
        }
        return message;
    }

}
