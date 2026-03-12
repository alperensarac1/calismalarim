package com.example.yardimuygulamajava.repo;

import com.example.yardimuygulamajava.model.HelpCancelBody;
import com.example.yardimuygulamajava.model.HelpConfirmBody;
import com.example.yardimuygulamajava.model.HelpCreateBody;
import com.example.yardimuygulamajava.model.HelpRequestActive;
import com.example.yardimuygulamajava.service.ApiClient;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.ApiService;

import retrofit2.Call;

public class PatientRepo {
    private final ApiService api = ApiClient.api();

    public Call<ApiOk<Object>> createHelp(long patientId, String servis, String oda, double lat, double lng) {
        return api.createHelp(new HelpCreateBody(patientId, servis, oda, lat, lng));
    }

    public Call<ApiOk<HelpRequestActive>> myActive(long patientId) {
        return api.myActive(patientId);
    }

    public Call<ApiOk<Object>> confirm(long requestId, long patientId) {
        return api.confirm(new HelpConfirmBody(requestId, patientId));
    }

    public Call<ApiOk<Object>> cancel(long requestId, long patientId) {
        return api.cancel(new HelpCancelBody(requestId, patientId));
    }
}
