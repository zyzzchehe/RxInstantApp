package com.rocktech.sharebookcase.msgdata;


import com.alibaba.fastjson.annotation.JSONType;

@JSONType(orders={"version","client","token","mode","method","url","action","formId","errorCallback"})
public class headMsg {

    private String version;
    private String client;

    private String token;
    private String mode;

    private String method;
    private String url;

    private String action;
    private String formId;

    private Integer errorCallback;



    public headMsg(){

        version = "1.0";
        client = "BMACH";
        token = "";
        mode = "remote";
        method = "POST";
        url = "chatRecord/save";
        action = "";
        formId = "";
        errorCallback = null;

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public Integer getErrorCallback() {
        return errorCallback;
    }

    public void setErrorCallback(Integer errorCallback) {
        this.errorCallback = errorCallback;
    }




}
