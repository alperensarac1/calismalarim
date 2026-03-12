package com.example.yardimuygulamajava.model;

public class HelpCreateBody {
    public long patient_id;
    public String servis_adi;
    public String oda_no;
    public double lat;
    public double lng;

    public HelpCreateBody(long patient_id, String servis_adi, String oda_no, double lat, double lng) {
        this.patient_id = patient_id;
        this.servis_adi = servis_adi;
        this.oda_no = oda_no;
        this.lat = lat;
        this.lng = lng;
    }
}
