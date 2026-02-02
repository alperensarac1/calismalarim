package com.example.kargopaylasimjava.dto;


import androidx.annotation.Nullable;

public class ApiResp<T> {
    public boolean ok;
    @Nullable public T data;
    @Nullable public String error;
}

