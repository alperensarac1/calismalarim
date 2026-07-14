package com.alperensarac.ebiletjava.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.Ticket;
import com.alperensarac.ebiletjava.data.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    MyTicketsActivity.java

    Kullanıcının satın aldığı biletleri listeler.

    Backend:
    tickets/my_tickets.php

    POST:
    api_token
*/
public class MyTicketsActivity extends AppCompatActivity {

    private Button btnBack;
    private TextView tvStatus;
    private RecyclerView rvTickets;

    private SessionManager sessionManager;

    private TicketAdapter ticketAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        sessionManager = new SessionManager(this);

        /*
            Token yoksa bu ekran kullanılmamalı.
        */
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();

        loadMyTickets();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvStatus = findViewById(R.id.tvStatus);
        rvTickets = findViewById(R.id.rvTickets);
    }

    private void setupRecyclerView() {
        ticketAdapter = new TicketAdapter(new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                /*
                    Bilete tıklanınca detay ekranına ticket_id gönderiyoruz.
                */
                Integer ticketId = ticket.getTicketId();

                if (ticketId == null || ticketId <= 0) {
                    Toast.makeText(
                            MyTicketsActivity.this,
                            "Bilet bilgisi alınamadı",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                Intent intent = new Intent(MyTicketsActivity.this, TicketDetailActivity.class);
                intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticketId);
                startActivity(intent);
            }
        });

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(ticketAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /*
        Kullanıcının biletlerini backend'den çeker.
    */
    private void loadMyTickets() {
        setStatus("Biletler yükleniyor...");

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getMyTickets(apiToken)
                .enqueue(new Callback<ApiResponse<List<Ticket>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Ticket>>> call,
                            Response<ApiResponse<List<Ticket>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            setStatus("Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<List<Ticket>> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("Boş sunucu cevabı");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            return;
                        }

                        List<Ticket> tickets = apiResponse.getData();

                        if (tickets == null) {
                            tickets = new ArrayList<>();
                        }

                        ticketAdapter.updateList(tickets);

                        if (tickets.isEmpty()) {
                            setStatus("Henüz satın alınmış biletin yok.");
                        } else {
                            setStatus(tickets.size() + " bilet listelendi.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Ticket>>> call, Throwable t) {
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Detay ekranından geri dönünce listeyi yeniliyoruz.
        İleride QR kontrol sonrası bilet used olabilir.
    */
    @Override
    protected void onResume() {
        super.onResume();

        if (ticketAdapter != null && sessionManager != null && sessionManager.isLoggedIn()) {
            loadMyTickets();
        }
    }

    private void setStatus(String message) {
        tvStatus.setText(message);
    }
}
