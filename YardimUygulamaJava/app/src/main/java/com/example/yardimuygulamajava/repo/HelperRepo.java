package com.example.yardimuygulamajava.repo;


import com.example.yardimuygulamajava.model.AcceptedHelpItem;
import com.example.yardimuygulamajava.model.ConfirmedHelpItem;
import com.example.yardimuygulamajava.model.HelpAcceptBody;
import com.example.yardimuygulamajava.model.OpenHelpItem;
import com.example.yardimuygulamajava.service.ApiClient;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.ApiService;

import retrofit2.Call;

public class HelperRepo {
    private final ApiService api = ApiClient.api();

    public Call<ApiOk<OpenHelpItem>> listOpen(long helperId) {
        return api.listOpen(helperId);
    }

    public Call<ApiOk<Object>> accept(long requestId, long helperId) {
        return api.accept(new HelpAcceptBody(requestId, helperId));
    }

    public Call<ApiOk<AcceptedHelpItem>> myAccepted(long helperId) {
        return api.myAccepted(helperId);
    }

    public Call<ApiOk<ConfirmedHelpItem>> myConfirmed(long helperId) {
        return api.myConfirmed(helperId);
    }
}