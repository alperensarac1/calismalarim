package com.example.yardimuygulamajava.view;


import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;


import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.adapter.OpenHelpAdapter;
import com.example.yardimuygulamajava.entity.Session;
import com.example.yardimuygulamajava.model.OpenHelpItem;
import com.example.yardimuygulamajava.repo.HelperRepo;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.Poller;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelperOpenListFragment extends Fragment {

    private long helperId;
    private final HelperRepo repo = new HelperRepo();

    private OpenHelpAdapter adapter;
    private Poller poller;

    public HelperOpenListFragment() {
        super(R.layout.fragment_helper_open_list);
    }

    public static HelperOpenListFragment newInstance(long helperId) {
        HelperOpenListFragment f = new HelperOpenListFragment();
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
        TextView tvSub = view.findViewById(R.id.tvSub);
        ProgressBar progress = view.findViewById(R.id.progress);

        Button btnAccepted = view.findViewById(R.id.btnAccepted);
        Button btnHistory  = view.findViewById(R.id.btnHistory);
        Button btnLogout   = view.findViewById(R.id.btnLogout);

        RecyclerView rv = view.findViewById(R.id.rvOpen);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new OpenHelpAdapter(item -> {
            progress.setVisibility(View.VISIBLE);
            repo.accept(item.id, helperId).enqueue(new Callback<ApiOk<Object>>() {
                @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                    progress.setVisibility(View.GONE);
                    ApiOk<Object> res = resp.body();
                    if (resp.isSuccessful() && res != null && res.getOk()) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, HelperAcceptedFragment.newInstance(helperId))
                                .addToBackStack(null)
                                .commit();
                    } else {
                        tvSub.setText("İstek alınamadı (başkası kabul etmiş olabilir)");
                    }
                }

                @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tvSub.setText("Hata: " + t.getMessage());
                }
            });
        });
        rv.setAdapter(adapter);

        btnAccepted.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, HelperAcceptedFragment.newInstance(helperId))
                        .addToBackStack(null)
                        .commit()
        );

        btnHistory.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, HelperConfirmedFragment.newInstance(helperId))
                        .addToBackStack(null)
                        .commit()
        );

        btnLogout.setOnClickListener(v -> {
            Session.clear(requireContext());
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new LoginFragment())
                    .commit();
        });

        poller = new Poller(4000, () -> repo.listOpen(helperId).enqueue(new Callback<ApiOk<OpenHelpItem>>() {
            @Override public void onResponse(Call<ApiOk<OpenHelpItem>> call, Response<ApiOk<OpenHelpItem>> resp) {
                ApiOk<OpenHelpItem> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk()) {
                    adapter.submit(res.items);
                    tvSub.setText("Bulunan: " + (res.items != null ? res.items.size() : 0));
                } else {
                    tvSub.setText(res != null && res.error != null ? res.error : "Liste alınamadı");
                }
            }

            @Override public void onFailure(Call<ApiOk<OpenHelpItem>> call, Throwable t) {
                tvSub.setText("Hata: " + t.getMessage());
            }
        }));
    }

    @Override public void onStart() {
        super.onStart();
        if (poller != null) poller.start();
    }

    @Override public void onStop() {
        super.onStop();
        if (poller != null) poller.stop();
    }
}