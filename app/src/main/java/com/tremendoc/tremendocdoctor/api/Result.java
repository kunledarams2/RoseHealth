package com.tremendoc.tremendocdoctor.api;

import java.util.List;

public class Result<T> {
    private boolean successful;
    private List<T> dataList;
    private T data;
    private String message;

    private void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setDataList(List<T> list) {
        this.dataList = list;
        setSuccessful(true);
    }

    public List<T> getDataList() {
        return dataList;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setMessage(String message) {
        this.message = message;
        this.successful = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg, boolean status) {
        setMessage(msg);
        setSuccessful(status);
    }
}
