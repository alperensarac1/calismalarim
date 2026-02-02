package com.example.kargopaylasimjava.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kargopaylasimjava.dto.ApiResp;
import com.example.kargopaylasimjava.dto.AuthDtos;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.repo.CargoRepository;
import com.example.kargopaylasimjava.service.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final CargoRepository repo;
    private final TokenStore tokenStore;

    private final MutableLiveData<UiState<Void>> _loginState = new MutableLiveData<>(UiState.idle());
    public LiveData<UiState<Void>> loginState = _loginState;

    private final MutableLiveData<UiState<Void>> _registerState = new MutableLiveData<>(UiState.idle());
    public LiveData<UiState<Void>> registerState = _registerState;

    public AuthViewModel(CargoRepository repo, TokenStore tokenStore) {
        this.repo = repo;
        this.tokenStore = tokenStore;
    }

    public void login(String phone, String password) {
        _loginState.setValue(UiState.loading());

        repo.login(phone, password).enqueue(new Callback<ApiResp<AuthDtos.LoginResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AuthDtos.LoginResp>> call,
                                   @NonNull Response<ApiResp<AuthDtos.LoginResp>> response) {
                ApiResp<AuthDtos.LoginResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok && body.data != null) {
                    tokenStore.saveToken(body.data.token);
                    _loginState.setValue(UiState.success(null));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Login failed";
                    _loginState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AuthDtos.LoginResp>> call, @NonNull Throwable t) {
                _loginState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    public void register(AuthDtos.RegisterReq req) {
        _registerState.setValue(UiState.loading());

        repo.register(req).enqueue(new Callback<ApiResp<AuthDtos.RegisterResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AuthDtos.RegisterResp>> call,
                                   @NonNull Response<ApiResp<AuthDtos.RegisterResp>> response) {
                ApiResp<AuthDtos.RegisterResp> body = response.body();
                if (response.isSuccessful() && body != null && body.ok) {
                    _registerState.setValue(UiState.success(null));
                } else {
                    String msg = (body != null && body.error != null) ? body.error : "Register failed";
                    _registerState.setValue(UiState.error(msg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AuthDtos.RegisterResp>> call, @NonNull Throwable t) {
                _registerState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }

    // Kotlin'deki registerAndSetup'in Java karşılığı (auto-login var)
    public void registerAndSetup(AuthDtos.RegisterReq registerReq) {
        _registerState.setValue(UiState.loading());

        repo.register(registerReq).enqueue(new Callback<ApiResp<AuthDtos.RegisterResp>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResp<AuthDtos.RegisterResp>> call,
                                   @NonNull Response<ApiResp<AuthDtos.RegisterResp>> response) {
                ApiResp<AuthDtos.RegisterResp> reg = response.body();
                if (!(response.isSuccessful() && reg != null && reg.ok)) {
                    String msg = (reg != null && reg.error != null) ? reg.error : "Register failed";
                    _registerState.setValue(UiState.error(msg));
                    return;
                }

                // auto login
                repo.login(registerReq.phone, registerReq.password).enqueue(new Callback<ApiResp<AuthDtos.LoginResp>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResp<AuthDtos.LoginResp>> call2,
                                           @NonNull Response<ApiResp<AuthDtos.LoginResp>> resp2) {
                        ApiResp<AuthDtos.LoginResp> login = resp2.body();
                        if (resp2.isSuccessful() && login != null && login.ok && login.data != null) {
                            tokenStore.saveToken(login.data.token);
                            _registerState.setValue(UiState.success(null));
                        } else {
                            String msg = (login != null && login.error != null) ? login.error : "Login after register failed";
                            _registerState.setValue(UiState.error(msg));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResp<AuthDtos.LoginResp>> call2, @NonNull Throwable t) {
                        _registerState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ApiResp<AuthDtos.RegisterResp>> call, @NonNull Throwable t) {
                _registerState.setValue(UiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
            }
        });
    }
}
