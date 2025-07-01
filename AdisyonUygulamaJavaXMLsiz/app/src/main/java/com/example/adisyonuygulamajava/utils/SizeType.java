package com.example.adisyonuygulamajava.utils;

import android.view.ViewGroup;

public enum SizeType {
    MATCH_PARENT,
    WRAP_CONTENT;

    public int toLayoutParam() {
        switch (this) {
            case MATCH_PARENT:
                return ViewGroup.LayoutParams.MATCH_PARENT;
            case WRAP_CONTENT:
                return ViewGroup.LayoutParams.WRAP_CONTENT;
            default:
                return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }
}

