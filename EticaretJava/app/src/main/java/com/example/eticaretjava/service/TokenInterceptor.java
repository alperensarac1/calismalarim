package com.example.eticaretjava.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    public interface TokenProvider {
        String getToken();
    }

    private final TokenProvider tokenProvider;

    public TokenInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String t = tokenProvider != null ? tokenProvider.getToken() : null;

        if (t != null && !t.trim().isEmpty()) {
            Request req = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + t)
                    .build();
            return chain.proceed(req);
        }
        return chain.proceed(original);
    }
}

