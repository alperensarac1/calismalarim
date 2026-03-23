package com.example.webtrafficviewerjava.model;

import java.util.HashMap;
import java.util.Map;

public class NetworkLog {

    private String method;
    private String url;
    private String host;
    private String time;
    private Map<String, String> headers;
    private boolean isMainFrame;
    private String resourceType;
    private String requestBody;
    private String source;

    public NetworkLog() {
        this.headers = new HashMap<>();
    }

    public NetworkLog(String method,
                      String url,
                      String host,
                      String time,
                      Map<String, String> headers,
                      boolean isMainFrame,
                      String resourceType,
                      String requestBody,
                      String source) {
        this.method = method;
        this.url = url;
        this.host = host;
        this.time = time;
        this.headers = headers != null ? headers : new HashMap<String, String>();
        this.isMainFrame = isMainFrame;
        this.resourceType = resourceType;
        this.requestBody = requestBody;
        this.source = source;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isMainFrame() {
        return isMainFrame;
    }

    public void setMainFrame(boolean mainFrame) {
        isMainFrame = mainFrame;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
