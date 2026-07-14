package com.alperensarac.ebiletkotlin.ui.ticket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.databinding.ItemTicketBinding
import com.bumptech.glide.Glide

/*
    TicketAdapter

    Biletlerim ekranındaki RecyclerView için kullanılır.

    Görevi:
    - Ticket listesini item_ticket.xml kartlarına basmak
    - Bilete tıklanınca TicketDetailActivity'ye geçmek için dışarı haber vermek
*/
class TicketAdapter(
    private val tickets: MutableList<Ticket>,
    private val onTicketClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(
        val binding: ItemTicketBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TicketViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        val binding = holder.binding

        val event = ticket.event

        binding.tvEventTitle.text = event?.title ?: ticket.eventTitle ?: "Etkinlik bilgisi yok"

        binding.tvDate.text = "Tarih: ${event?.eventDate ?: "-"}"

        binding.tvVenue.text = "Sahne: ${ticket.location?.venueName ?: "-"}"

        val cityName = ticket.location?.cityName ?: "-"
        val districtName = ticket.location?.districtName ?: "-"
        binding.tvLocation.text = "$cityName / $districtName"

        binding.tvPrice.text = "${ticket.price?.toInt() ?: 0} TL"

        /*
            Backend status değerleri:
            active
            used
            cancelled
        */
        val status = ticket.status ?: ticket.ticketStatus ?: "-"

        val statusText = when (status) {
            "active" -> "Aktif"
            "used" -> "Kullanıldı"
            "cancelled" -> "İptal"
            else -> status
        }

        binding.tvStatusBadge.text = statusText

        /*
            Renkleri basitçe ayırıyoruz.
            XML background düz olduğu için TextView arka plan rengi set ediyoruz.
        */
        when (status) {
            "active" -> {
                binding.tvStatusBadge.setBackgroundColor(0xFFDCFCE7.toInt())
                binding.tvStatusBadge.setTextColor(0xFF166534.toInt())
            }

            "used" -> {
                binding.tvStatusBadge.setBackgroundColor(0xFFE2E8F0.toInt())
                binding.tvStatusBadge.setTextColor(0xFF475569.toInt())
            }

            "cancelled" -> {
                binding.tvStatusBadge.setBackgroundColor(0xFFFEE2E2.toInt())
                binding.tvStatusBadge.setTextColor(0xFF991B1B.toInt())
            }

            else -> {
                binding.tvStatusBadge.setBackgroundColor(0xFFEFF6FF.toInt())
                binding.tvStatusBadge.setTextColor(0xFF2563EB.toInt())
            }
        }

        /*
            Poster yükleme.
        */
        val baseUrl = "http://10.0.2.2/event_ticket_api/"
        val posterUrl = event?.posterUrl

        if (!posterUrl.isNullOrEmpty()) {
            val finalPosterUrl = if (posterUrl.startsWith("http")) {
                posterUrl
            } else {
                baseUrl + posterUrl
            }

            Glide.with(binding.imgPoster.context)
                .load(finalPosterUrl)
                .centerCrop()
                .into(binding.imgPoster)
        } else {
            binding.imgPoster.setImageDrawable(null)
        }

        binding.itemTicketRoot.setOnClickListener {
            onTicketClick(ticket)
        }
    }

    fun updateList(newTickets: List<Ticket>) {
        tickets.clear()
        tickets.addAll(newTickets)
        notifyDataSetChanged()
    }
}