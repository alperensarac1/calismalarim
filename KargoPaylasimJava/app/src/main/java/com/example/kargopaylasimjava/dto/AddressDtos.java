package com.example.kargopaylasimjava.dto;

import java.util.List;

public class AddressDtos {

    public static class AddressCreateReq {
        public String title;
        public String city;
        public String district;
        public String neighborhood;  // nullable
        public String address_line;
        public String postal_code;   // nullable

        public AddressCreateReq(String title, String city, String district,
                                String neighborhood, String address_line, String postal_code) {
            this.title = title;
            this.city = city;
            this.district = district;
            this.neighborhood = neighborhood;
            this.address_line = address_line;
            this.postal_code = postal_code;
        }
    }

    public static class AddressDetailReq {
        public int id;
        public String token;
        public AddressDetailReq(int id, String token) {
            this.id = id; this.token = token;
        }
    }

    public static class AddressUpdateReq {
        public int id;
        public String title;
        public String city;
        public String district;
        public String neighborhood; // nullable
        public String address_line;
        public String postal_code;  // nullable

        public AddressUpdateReq(int id, String title, String city, String district,
                                String neighborhood, String address_line, String postal_code) {
            this.id = id;
            this.title = title;
            this.city = city;
            this.district = district;
            this.neighborhood = neighborhood;
            this.address_line = address_line;
            this.postal_code = postal_code;
        }
    }

    public static class AddressDto {
        public int id;
        public String title;
        public String city;
        public String district;
        public String neighborhood; // nullable
        public String address_line;
        public String postal_code;  // nullable
        public int is_default;
    }

    public static class ReceiverAddressDto {
        public int id;
        public String title;
        public String city;
        public String district;
        public String address_line;
    }

    public static class AddressDeleteReq {
        public int id;
        public AddressDeleteReq(int id) { this.id = id; }
    }

    public static class AddressSetDefaultReq {
        public int id;
        public AddressSetDefaultReq(int id) { this.id = id; }
    }

    public static class AddressListResp {
        public List<AddressDto> items;
    }

    public static class AddressIdResp {
        public int id;
    }
}

