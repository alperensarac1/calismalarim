package com.alperensarac.ebiletkotlin.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.ebiletkotlin.data.model.Event
import com.alperensarac.ebiletkotlin.databinding.ItemEventBinding
import com.bumptech.glide.Glide

/*
    EventAdapter

    RecyclerView içinde etkinlikleri göstermek için kullanılır.

    Adapter'ın görevi:
    - Elindeki Event listesini alır.
    - Her Event için item_event.xml tasarımını doldurur.
    - Kullanıcı karta tıklarsa dışarıya haber verir.

    onEventClick:
    HomeActivity'den gelen tıklama fonksiyonudur.
    Bir etkinliğe basılınca etkinlik detayına geçmek için kullanacağız.
*/
class EventAdapter(
    private val events: MutableList<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    /*
        ViewHolder:
        RecyclerView içindeki tek kartın view referanslarını tutar.
    */
    inner class EventViewHolder(
        val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root)

    /*
        RecyclerView yeni bir kart görünümü oluşturmak istediğinde çalışır.
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return EventViewHolder(binding)
    }

    /*
        Listedeki kaç eleman olduğunu söyler.
    */
    override fun getItemCount(): Int {
        return events.size
    }

    /*
        Her kart ekranda görüneceği zaman burası çalışır.
        Event verisini XML üzerindeki TextView/ImageView alanlarına basar.
    */
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val binding = holder.binding

        binding.tvEventTitle.text = event.title

        /*
            Venue bilgisi listeleme API'sinde nested olarak geliyor.
            Eğer null ise boş metin yerine daha kontrollü gösteriyoruz.
        */
        val venueName = event.venue?.name ?: "Sahne bilgisi yok"
        binding.tvVenue.text = "Sahne: $venueName"

        val cityName = event.cityName ?: event.city?.name ?: "-"
        val districtName = event.districtName ?: event.district?.name ?: "-"

        binding.tvLocation.text = "$cityName / $districtName"

        binding.tvEventDate.text = "Tarih: ${event.eventDate ?: "-"}"
        binding.tvPrice.text = "${event.basePrice?.toInt() ?: 0} TL"
        binding.tvQuota.text = "Kalan: ${event.remainingQuota ?: 0}"

        /*
            Poster URL konusu:

            Backend bize şunu döndürüyor olabilir:
            uploads/events/kadikoy_akustik.jpg

            Glide tam URL ister:
            http://10.0.2.2/event_ticket_api/uploads/events/kadikoy_akustik.jpg

            Basitlik için burada poster_url zaten tam URL değilse base path ekliyoruz.

            Not:
            ApiClient içindeki BASE_URL private olduğu için burada tekrar yazdık.
            İleride Constants.kt yapıp tek yerden yönetebiliriz.
        */
        val baseUrl = "http://10.0.2.2/event_ticket_api/"

        val posterUrl = event.posterUrl

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
            /*
                Poster yoksa ImageView'i boş bırakıyoruz.
                İleride placeholder drawable ekleyebiliriz.
            */
            binding.imgPoster.setImageDrawable(null)
        }

        /*
            Karta tıklanınca HomeActivity'ye haber veriyoruz.
        */
        binding.itemEventRoot.setOnClickListener {
            onEventClick(event)
        }
    }

    /*
        Listeyi dışarıdan güncellemek için yardımcı fonksiyon.

        Yeni etkinlikler geldiğinde:
        - eski liste temizlenir
        - yeni liste eklenir
        - RecyclerView yenilenir
    */
    fun updateList(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}