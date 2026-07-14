package com.alperensarac.ebiletkotlin.ui.ticket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityMyTicketsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    MyTicketsActivity

    Kullanıcının satın aldığı biletleri listeler.

    Backend:
    tickets/my_tickets.php

    POST:
    - api_token
*/
class MyTicketsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyTicketsBinding

    private lateinit var sessionManager: SessionManager

    private lateinit var ticketAdapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyTicketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()

        loadMyTickets()
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(
            tickets = mutableListOf(),
            onTicketClick = { ticket ->

                val ticketId = ticket.ticketId

                if (ticketId == null || ticketId <= 0) {
                    Toast.makeText(this, "Bilet bilgisi alınamadı", Toast.LENGTH_SHORT).show()
                    return@TicketAdapter
                }

                val intent = Intent(this, TicketDetailActivity::class.java)
                intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticketId)
                startActivity(intent)
            }
        )

        binding.rvTickets.layoutManager = LinearLayoutManager(this)
        binding.rvTickets.adapter = ticketAdapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /*
        Biletlerim listesini backend'den çeker.
    */
    private fun loadMyTickets() {
        setStatus("Biletler yükleniyor...")

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getMyTickets(apiToken)
            .enqueue(object : Callback<ApiResponse<List<Ticket>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<Ticket>>>,
                    response: Response<ApiResponse<List<Ticket>>>
                ) {
                    if (!response.isSuccessful) {
                        setStatus("Sunucu hatası: ${response.code()}")
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        setStatus("Boş sunucu cevabı")
                        return
                    }

                    if (!apiResponse.success) {
                        setStatus(apiResponse.message)
                        return
                    }

                    val tickets = apiResponse.data ?: emptyList()

                    ticketAdapter.updateList(tickets)

                    if (tickets.isEmpty()) {
                        setStatus("Henüz satın alınmış biletin yok.")
                    } else {
                        setStatus("${tickets.size} bilet listelendi.")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Ticket>>>,
                    t: Throwable
                ) {
                    setStatus("Bağlantı hatası: ${t.localizedMessage}")
                }
            })
    }

    /*
        Detay ekranından geri dönüldüğünde listeyi yeniliyoruz.
        Çünkü QR kontrol sonrası bilet status used olabilir.
    */
    override fun onResume() {
        super.onResume()

        if (::ticketAdapter.isInitialized && sessionManager.isLoggedIn()) {
            loadMyTickets()
        }
    }

    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }
}