package com.example.yardimuygulamajava.model;

public class HelpConfirmBody {
    public long request_id;
    public long patient_id;

    public HelpConfirmBody(long request_id, long patient_id) {
        this.request_id = request_id;
        this.patient_id = patient_id;
    }
}
