package com.digicomme.tremendocdoctor.model;

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
}
