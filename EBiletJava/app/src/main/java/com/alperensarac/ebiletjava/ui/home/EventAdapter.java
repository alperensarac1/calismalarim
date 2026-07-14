package com.alperensarac.ebiletjava.ui.home;


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
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/*
    EventAdapter.java

    RecyclerView içinde etkinlikleri göstermek için kullanılır.

    Görevi:
    - Event listesini item_event.xml tasarımına bağlar.
    - Her etkinlik kartını doldurur.
    - Kullanıcı karta tıklarsa HomeActivity'ye haber verir.

    Java tarafında Kotlin'deki lambda yerine interface kullanıyoruz.
*/
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /*
        Etkinlik listesi.
    */
    private final List<Event> eventList = new ArrayList<>();

    /*
        Kart tıklama olayını dışarı aktarmak için interface.
    */
    private final OnEventClickListener listener;

    /*
        Constructor.
    */
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /*
        Kart tıklama interface'i.
        HomeActivity bunu implemente etmeden anonymous class ile kullanacak.
    */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /*
        ViewHolder oluşturulur.
    */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

    /*
        Her kartın içi doldurulur.
    */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());

        String eventDate = event.getEventDate() == null ? "-" : event.getEventDate();
        holder.tvEventDate.setText("Tarih: " + eventDate);

        String venueName = "-";

        if (event.getVenue() != null && event.getVenue().getName() != null) {
            venueName = event.getVenue().getName();
        }

        holder.tvVenue.setText("Sahne: " + venueName);

        String cityName = "-";
        String districtName = "-";

        if (event.getCityName() != null) {
            cityName = event.getCityName();
        } else if (event.getCity() != null && event.getCity().getName() != null) {
            cityName = event.getCity().getName();
        }

        if (event.getDistrictName() != null) {
            districtName = event.getDistrictName();
        } else if (event.getDistrict() != null && event.getDistrict().getName() != null) {
            districtName = event.getDistrict().getName();
        }

        holder.tvLocation.setText(cityName + " / " + districtName);

        int price = 0;

        if (event.getBasePrice() != null) {
            price = event.getBasePrice().intValue();
        }

        holder.tvPrice.setText(price + " TL");

        int remainingQuota = 0;

        if (event.getRemainingQuota() != null) {
            remainingQuota = event.getRemainingQuota();
        }

        holder.tvQuota.setText("Kalan: " + remainingQuota);

        /*
            Poster görseli.

            Backend poster_url örnek:
            uploads/events/kadikoy_akustik.jpg

            Tam URL:
            http://10.0.2.2/event_ticket_api/uploads/events/kadikoy_akustik.jpg
        */
        String posterUrl = event.getPosterUrl();

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
                listener.onEventClick(event);
            }
        });
    }

    /*
        Liste eleman sayısı.
    */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /*
        Dışarıdan yeni liste geldiğinde adapter listesini günceller.
    */
    public void updateList(List<Event> newList) {
        eventList.clear();

        if (newList != null) {
            eventList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    /*
        ViewHolder:
        item_event.xml içindeki view referanslarını tutar.
    */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView tvEventTitle;
        TextView tvEventDate;
        TextView tvVenue;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvQuota;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuota = itemView.findViewById(R.id.tvQuota);
        }
    }
}
