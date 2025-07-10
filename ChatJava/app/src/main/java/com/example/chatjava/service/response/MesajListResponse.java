package com.example.chatjava.service.response;

import com.example.chatjava.model.Mesaj;

import java.util.List;

public class MesajListResponse {
    private boolean success;
    private List<Mesaj> mesajlar;

    public MesajListResponse(boolean success, List<Mesaj> mesajlar) {
        this.success = success;
        this.mesajlar = mesajlar;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Mesaj> getMesajlar() {
        return mesajlar;
    }
}

