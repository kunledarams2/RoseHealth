package com.digicomme.tremendocdoctor.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Tip {
    private String title, summary, body, image;
    private int id, likes;

    public Tip() {
        setId(0);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikes() {
        return likes;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getImage() {
        return image;
    }


    public static Tip parse(JSONObject object) throws JSONException {
        Tip tip = new Tip();
        tip.setBody(object.getString("body"));
        tip.setImage(object.getString("image"));
        tip.setLikes(object.getInt("likes"));
        tip.setSummary(object.getString("summary"));
        tip.setTitle(object.getString("title"));
        return tip;
    }

}
