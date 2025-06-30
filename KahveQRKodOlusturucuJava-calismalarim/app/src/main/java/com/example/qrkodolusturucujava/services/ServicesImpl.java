package com.example.qrkodolusturucujava.services;

import com.example.qrkodolusturucujava.services.dummyservice.KahveDummyServisDao;
import com.example.qrkodolusturucujava.services.retrofit.KahveHTTPServisDao;

public class ServicesImpl {
    public Services getInstance() {
        return new KahveHTTPServisDao();
    }
}
