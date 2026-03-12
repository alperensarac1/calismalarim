package com.example.yardimuygulamajava.service;

import java.util.List;

public class ApiOk<T> {
    public Boolean ok;
    public String error;

    public User user;

    public Integer count;
    public List<T> items;

    public T active; // myActive için

    public Boolean getOk() { return ok != null && ok; }
    public String getError() { return error; }
    public User getUser() { return user; }
    public List<T> getItems() { return items; }
    public T getActive() { return active; }
}
