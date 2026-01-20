package com.example.eticaretjava.repo;

public interface ResultCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}

