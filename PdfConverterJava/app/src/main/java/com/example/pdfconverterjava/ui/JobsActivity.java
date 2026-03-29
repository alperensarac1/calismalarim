package com.example.pdfconverterjava.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pdfconverterjava.R;
import com.example.pdfconverterjava.data.model.JobItem;
import com.example.pdfconverterjava.data.model.ListJobsResponse;
import com.example.pdfconverterjava.data.repository.PdfRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobsActivity extends AppCompatActivity {

    private RecyclerView recyclerJobs;
    private ProgressBar progressBarJobs;
    private TextView tvEmpty;

    private JobHistoryAdapter adapter;
    private PdfRepository repository;

    // Şimdilik sabit kullanıcı id
    private final int userId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        repository = new PdfRepository();

        initViews();
        initRecycler();
        loadJobs();
    }

    private void initViews() {
        recyclerJobs = findViewById(R.id.recyclerJobs);
        progressBarJobs = findViewById(R.id.progressBarJobs);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void initRecycler() {
        adapter = new JobHistoryAdapter(new ArrayList<>());

        recyclerJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerJobs.setAdapter(adapter);
    }

    private void loadJobs() {
        progressBarJobs.setVisibility(View.VISIBLE);
        recyclerJobs.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        repository.listJobs(userId).enqueue(new Callback<ListJobsResponse>() {
            @Override
            public void onResponse(Call<ListJobsResponse> call, Response<ListJobsResponse> response) {
                progressBarJobs.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ListJobsResponse body = response.body();

                    if (body.isSuccess() && body.getJobs() != null && !body.getJobs().isEmpty()) {
                        List<JobItem> jobs = body.getJobs();
                        adapter.submitList(jobs);

                        recyclerJobs.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        recyclerJobs.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerJobs.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Liste alınamadı. Hata kodu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ListJobsResponse> call, Throwable t) {
                progressBarJobs.setVisibility(View.GONE);
                recyclerJobs.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Hata: " + t.getMessage());
            }
        });
    }
}
