package com.example.yardimuygulamajava.repo;

import com.example.yardimuygulamajava.service.ApiClient;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.ApiService;
import com.example.yardimuygulamajava.service.LoginBody;
import com.example.yardimuygulamajava.service.RegisterBody;

import retrofit2.Call;

public class AuthRepo {
    private final ApiService api = ApiClient.api();

    public Call<ApiOk<Object>> login(String phone, String pass) {
        return api.login(new LoginBody(phone, pass));
    }

    public Call<ApiOk<Object>> register(RegisterBody body) {
        return api.register(body);
    }
}
