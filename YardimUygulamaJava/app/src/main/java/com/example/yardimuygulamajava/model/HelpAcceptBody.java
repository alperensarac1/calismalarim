package com.example.yardimuygulamajava.model;

public class HelpAcceptBody {
    public long request_id;
    public long helper_id;

    public HelpAcceptBody(long request_id, long helper_id) {
        this.request_id = request_id;
        this.helper_id = helper_id;
    }
}
