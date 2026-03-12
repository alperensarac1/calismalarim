package com.example.yardimuygulamajava.view;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.adapter.ConfirmedHelpAdapter;
import com.example.yardimuygulamajava.model.ConfirmedHelpItem;
import com.example.yardimuygulamajava.repo.HelperRepo;
import com.example.yardimuygulamajava.service.ApiOk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelperConfirmedFragment extends Fragment {

    private long helperId;
    private final HelperRepo repo = new HelperRepo();

    private ConfirmedHelpAdapter adapter;
    private TextView tvInfo;

    public HelperConfirmedFragment() {
        super(R.layout.fragment_helper_confirmed);
    }

    public static HelperConfirmedFragment newInstance(long helperId) {
        HelperConfirmedFragment f = new HelperConfirmedFragment();
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
        tvInfo = view.findViewById(R.id.tvInfo);

        RecyclerView rv = view.findViewById(R.id.rvConfirmed);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ConfirmedHelpAdapter();
        rv.setAdapter(adapter);

        loadOnce();
    }

    private void loadOnce() {
        tvInfo.setText("Yükleniyor...");

        repo.myConfirmed(helperId).enqueue(new Callback<ApiOk<ConfirmedHelpItem>>() {
            @Override public void onResponse(Call<ApiOk<ConfirmedHelpItem>> call, Response<ApiOk<ConfirmedHelpItem>> resp) {
                ApiOk<ConfirmedHelpItem> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk()) {
                    adapter.submit(res.items);
                    int c = res.items != null ? res.items.size() : 0;
                    tvInfo.setText("Toplam: " + c);
                } else {
                    tvInfo.setText(res != null && res.error != null ? res.error : "Geçmiş alınamadı");
                }
            }

            @Override public void onFailure(Call<ApiOk<ConfirmedHelpItem>> call, Throwable t) {
                tvInfo.setText("Hata: " + t.getMessage());
            }
        });
    }
}