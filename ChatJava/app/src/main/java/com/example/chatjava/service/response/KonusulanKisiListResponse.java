package com.example.chatjava.service.response;

import com.example.chatjava.model.KonusulanKisi;

import java.util.List;

public class KonusulanKisiListResponse {
    private boolean success;
    private List<KonusulanKisi> kisiler;

    public KonusulanKisiListResponse(boolean success, List<KonusulanKisi> kisiler) {
        this.success = success;
        this.kisiler = kisiler;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<KonusulanKisi> getKisiler() {
        return kisiler;
    }
}

