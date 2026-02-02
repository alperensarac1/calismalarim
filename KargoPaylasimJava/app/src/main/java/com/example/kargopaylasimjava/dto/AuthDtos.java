package com.example.kargopaylasimjava.dto;

public class AuthDtos {

    public static class RegisterReq {
        public String first_name;
        public String last_name;
        public String phone;
        public String tc_no;
        public String password;

        // address (register endpoint default adres de oluşturuyor)
        public String address_title;
        public String city;
        public String district;
        public String neighborhood; // nullable
        public String address_line;
        public String postal_code;  // nullable

        public RegisterReq(
                String first_name, String last_name, String phone, String tc_no, String password,
                String address_title, String city, String district, String neighborhood,
                String address_line, String postal_code
        ) {
            this.first_name = first_name;
            this.last_name = last_name;
            this.phone = phone;
            this.tc_no = tc_no;
            this.password = password;
            this.address_title = address_title;
            this.city = city;
            this.district = district;
            this.neighborhood = neighborhood;
            this.address_line = address_line;
            this.postal_code = postal_code;
        }
    }

    public static class RegisterResp {
        public int user_id;
        public int address_id;
    }

    public static class LoginReq {
        public String phone;
        public String password;

        public LoginReq(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }
    }

    public static class LoginResp {
        public String token;
    }

    public static class UserMeResp {
        public UserDto user;
    }

    public static class UserDto {
        public int id;
        public String first_name;
        public String last_name;
        public String phone_e164;
    }
}

