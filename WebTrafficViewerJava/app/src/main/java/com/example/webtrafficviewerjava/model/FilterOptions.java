package com.example.webtrafficviewerjava.model;

public class FilterOptions {

    private boolean enableFilter;
    private boolean onlyApiRequests;
    private boolean enableJsHook;
    private boolean showOnlyGet;
    private boolean showOnlyPost;
    private String searchQuery;

    public FilterOptions() {
        this.enableFilter = true;
        this.onlyApiRequests = false;
        this.enableJsHook = true;
        this.showOnlyGet = false;
        this.showOnlyPost = false;
        this.searchQuery = "";
    }

    public boolean isEnableFilter() {
        return enableFilter;
    }

    public void setEnableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
    }

    public boolean isOnlyApiRequests() {
        return onlyApiRequests;
    }

    public void setOnlyApiRequests(boolean onlyApiRequests) {
        this.onlyApiRequests = onlyApiRequests;
    }

    public boolean isEnableJsHook() {
        return enableJsHook;
    }

    public void setEnableJsHook(boolean enableJsHook) {
        this.enableJsHook = enableJsHook;
    }

    public boolean isShowOnlyGet() {
        return showOnlyGet;
    }

    public void setShowOnlyGet(boolean showOnlyGet) {
        this.showOnlyGet = showOnlyGet;
    }

    public boolean isShowOnlyPost() {
        return showOnlyPost;
    }

    public void setShowOnlyPost(boolean showOnlyPost) {
        this.showOnlyPost = showOnlyPost;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery : "";
    }
}
