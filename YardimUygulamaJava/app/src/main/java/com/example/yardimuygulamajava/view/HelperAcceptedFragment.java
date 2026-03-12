package com.example.yardimuygulamajava.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.model.AcceptedHelpItem;
import com.example.yardimuygulamajava.repo.HelperRepo;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.Poller;
import com.example.yardimuygulamajava.util.TimeUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelperAcceptedFragment extends Fragment {

    private long helperId;
    private final HelperRepo repo = new HelperRepo();

    private TextView tvPatient, tvPhone, tvService, tvRoom, tvTimer, tvInfo;
    private ProgressBar progress;
    private Button btnCall;

    private Poller poller;
    private boolean hadAcceptedBefore = false;
    private AcceptedHelpItem current = null;

    public HelperAcceptedFragment() {
        super(R.layout.fragment_helper_accepted);
    }

    public static HelperAcceptedFragment newInstance(long helperId) {
        HelperAcceptedFragment f = new HelperAcceptedFragment();
        Bundle b = new Bundle();
        b.putLong("helper_id", helperId);
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helperId = requireArguments().getLong("helper_id");
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        tvPatient = view.findViewById(R.id.tvPatient);
        tvPhone   = view.findViewById(R.id.tvPhone);
        tvService = view.findViewById(R.id.tvService);
        tvRoom    = view.findViewById(R.id.tvRoom);
        tvTimer   = view.findViewById(R.id.tvTimer);
        tvInfo    = view.findViewById(R.id.tvInfo);
        progress  = view.findViewById(R.id.progress);
        btnCall   = view.findViewById(R.id.btnDial);

        btnCall.setOnClickListener(v -> {
            if (current == null || current.patient_phone == null) return;
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + current.patient_phone));
            startActivity(i);
        });

        poller = new Poller(2000, this::fetchAcceptedOnce);
    }

    @Override public void onStart() {
        super.onStart();
        if (poller != null) poller.start();
    }

    @Override public void onStop() {
        super.onStop();
        if (poller != null) poller.stop();
    }

    private void fetchAcceptedOnce() {
        repo.myAccepted(helperId).enqueue(new Callback<ApiOk<AcceptedHelpItem>>() {
            @Override public void onResponse(Call<ApiOk<AcceptedHelpItem>> call, Response<ApiOk<AcceptedHelpItem>> resp) {
                ApiOk<AcceptedHelpItem> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk() && res.items != null && !res.items.isEmpty()) {
                    hadAcceptedBefore = true;
                    current = res.items.get(0);

                    progress.setVisibility(View.GONE);
                    tvInfo.setText("");

                    tvPatient.setText("Hasta: " + safe(current.patient_name) + " (" + (current.patient_age != null ? current.patient_age : "-") + ")");
                    tvPhone.setText("Telefon: " + safe(current.patient_phone));
                    tvService.setText("Servis: " + safe(current.servis_adi));
                    tvRoom.setText("Oda: " + safe(current.oda_no));
                    tvTimer.setText("Kalan süre: " + TimeUtils.formatRemainingSeconds(current.remaining_seconds));

                    btnCall.setEnabled(current.patient_phone != null && !current.patient_phone.isEmpty());

                } else {
                    // accepted artık yok -> confirmed veya timeout
                    if (hadAcceptedBefore) {
                        Toast.makeText(requireContext(), "Hasta onayladı veya süre doldu. Listeye dönülüyor.", Toast.LENGTH_SHORT).show();
                    }
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override public void onFailure(Call<ApiOk<AcceptedHelpItem>> call, Throwable t) {
                tvInfo.setText("Hata: " + t.getMessage());
            }
        });
    }

    private String safe(String s) { return s != null ? s : "-"; }
}
