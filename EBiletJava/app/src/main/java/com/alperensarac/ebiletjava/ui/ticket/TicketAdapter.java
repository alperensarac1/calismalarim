package com.alperensarac.ebiletjava.ui.ticket;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.Event;
import com.alperensarac.ebiletjava.data.model.Ticket;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/*
    TicketAdapter.java

    Biletlerim ekranındaki RecyclerView için kullanılır.

    Görevi:
    - Ticket listesini item_ticket.xml kartlarına basar.
    - Kullanıcı bilete tıklarsa TicketDetailActivity ekranına geçmek için haber verir.
*/
public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<Ticket> ticketList = new ArrayList<>();

    private final OnTicketClickListener listener;

    public TicketAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    /*
        Bilet kartına tıklanınca çalışacak interface.
    */
    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);

        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        Event event = ticket.getEvent();

        /*
            Etkinlik adı.
            Bazı API cevaplarında event nested gelir, bazılarında event_title gelir.
        */
        String eventTitle = "Etkinlik bilgisi yok";

        if (event != null && event.getTitle() != null) {
            eventTitle = event.getTitle();
        } else if (ticket.getEventTitle() != null) {
            eventTitle = ticket.getEventTitle();
        }

        holder.tvEventTitle.setText(eventTitle);

        /*
            Tarih.
        */
        String eventDate = "-";

        if (event != null && event.getEventDate() != null) {
            eventDate = event.getEventDate();
        }

        holder.tvDate.setText("Tarih: " + eventDate);

        /*
            Location bilgisi my_tickets.php cevabındaki location objesinden gelir.
        */
        String venueName = "-";
        String cityName = "-";
        String districtName = "-";

        if (ticket.getLocation() != null) {
            if (ticket.getLocation().getVenueName() != null) {
                venueName = ticket.getLocation().getVenueName();
            }

            if (ticket.getLocation().getCityName() != null) {
                cityName = ticket.getLocation().getCityName();
            }

            if (ticket.getLocation().getDistrictName() != null) {
                districtName = ticket.getLocation().getDistrictName();
            }
        }

        holder.tvVenue.setText("Sahne: " + venueName);
        holder.tvLocation.setText(cityName + " / " + districtName);

        /*
            Fiyat.
        */
        int price = 0;

        if (ticket.getPrice() != null) {
            price = ticket.getPrice().intValue();
        }

        holder.tvPrice.setText(price + " TL");

        /*
            Bilet durumu.
            Backend:
            active
            used
            cancelled
        */
        String status = "-";

        if (ticket.getStatus() != null) {
            status = ticket.getStatus();
        } else if (ticket.getTicketStatus() != null) {
            status = ticket.getTicketStatus();
        }

        String statusText;

        if ("active".equals(status)) {
            statusText = "Aktif";
            holder.tvStatusBadge.setBackgroundColor(0xFFDCFCE7);
            holder.tvStatusBadge.setTextColor(0xFF166534);
        } else if ("used".equals(status)) {
            statusText = "Kullanıldı";
            holder.tvStatusBadge.setBackgroundColor(0xFFE2E8F0);
            holder.tvStatusBadge.setTextColor(0xFF475569);
        } else if ("cancelled".equals(status)) {
            statusText = "İptal";
            holder.tvStatusBadge.setBackgroundColor(0xFFFEE2E2);
            holder.tvStatusBadge.setTextColor(0xFF991B1B);
        } else {
            statusText = status;
            holder.tvStatusBadge.setBackgroundColor(0xFFEFF6FF);
            holder.tvStatusBadge.setTextColor(0xFF2563EB);
        }

        holder.tvStatusBadge.setText(statusText);

        /*
            Poster yükleme.
            Poster event nesnesinin içinden gelir.
        */
        String posterUrl = null;

        if (event != null) {
            posterUrl = event.getPosterUrl();
        }

        if (posterUrl != null && !posterUrl.isEmpty()) {
            String finalPosterUrl;

            if (posterUrl.startsWith("http")) {
                finalPosterUrl = posterUrl;
            } else {
                finalPosterUrl = ApiClient.getBaseUrl() + posterUrl;
            }

            Glide.with(holder.imgPoster.getContext())
                    .load(finalPosterUrl)
                    .centerCrop()
                    .into(holder.imgPoster);
        } else {
            holder.imgPoster.setImageDrawable(null);
        }

        /*
            Kart tıklama.
        */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTicketClick(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    /*
        Dışarıdan gelen listeyi adapter'a basar.
    */
    public void updateList(List<Ticket> newList) {
        ticketList.clear();

        if (newList != null) {
            ticketList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    /*
        ViewHolder.
    */
    static class TicketViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView tvEventTitle;
        TextView tvDate;
        TextView tvVenue;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvStatusBadge;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
