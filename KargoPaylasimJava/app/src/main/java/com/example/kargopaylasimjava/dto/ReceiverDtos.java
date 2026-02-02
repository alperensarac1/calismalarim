package com.example.kargopaylasimjava.dto;

public class ReceiverDtos {

    public static class ReceiverLookupReq {
        public String phone;
        public ReceiverLookupReq(String phone) { this.phone = phone; }
    }

    public static class ReceiverLookupResp {
        public int receiver_user_id;
        public String masked_first_name;
        public String masked_last_name;
    }
}

