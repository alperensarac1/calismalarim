package com.alperensarac.ebiletjava.data.model;

import com.google.gson.annotations.SerializedName;

/*
    Ticket.java

    Bilet modelidir.

    Bu model şu API'lerde kullanılır:
    - ticket_buy.php
    - my_tickets.php
    - ticket_detail.php
    - ticket_check.php

    Farklı API'lerden gelen cevaplar biraz farklı olabileceği için
    birçok alan nullable yani nesne tipinde tutuldu.
*/
public class Ticket {

    @SerializedName("ticket_id")
    private Integer ticketId;

    @SerializedName("event_id")
    private Integer eventId;

    @SerializedName("event_title")
    private String eventTitle;

    @SerializedName("ticket_code")
    private String ticketCode;

    @SerializedName("qr_code_text")
    private String qrCodeText;

    private Double price;

    /*
        active
        used
        cancelled
    */
    private String status;

    @SerializedName("ticket_status")
    private String ticketStatus;

    @SerializedName("purchased_at")
    private String purchasedAt;

    @SerializedName("used_at")
    private String usedAt;

    @SerializedName("transaction_id")
    private String transactionId;

    private Event event;

    private City city;

    private District district;

    private Venue venue;

    private TicketLocation location;

    private User user;

    @SerializedName("checked_by")
    private CheckedBy checkedBy;

    private String result;

    public Integer getTicketId() {
        return ticketId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getQrCodeText() {
        return qrCodeText;
    }

    public Double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public String getPurchasedAt() {
        return purchasedAt;
    }

    public String getUsedAt() {
        return usedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Event getEvent() {
        return event;
    }

    public City getCity() {
        return city;
    }

    public District getDistrict() {
        return district;
    }

    public Venue getVenue() {
        return venue;
    }

    public TicketLocation getLocation() {
        return location;
    }

    public User getUser() {
        return user;
    }

    public CheckedBy getCheckedBy() {
        return checkedBy;
    }

    public String getResult() {
        return result;
    }

    /*
        İç içe location modeli.

        Backend my_tickets.php ve ticket_check.php içinde:
        "location": {
            "city_name": "...",
            "district_name": "...",
            "venue_name": "...",
            "venue_address": "..."
        }
    */
    public static class TicketLocation {

        @SerializedName("city_name")
        private String cityName;

        @SerializedName("district_name")
        private String districtName;

        @SerializedName("venue_name")
        private String venueName;

        @SerializedName("venue_address")
        private String venueAddress;

        public String getCityName() {
            return cityName;
        }

        public String getDistrictName() {
            return districtName;
        }

        public String getVenueName() {
            return venueName;
        }

        public String getVenueAddress() {
            return venueAddress;
        }
    }

    /*
        QR kontrolü yapan görevli bilgisi.
    */
    public static class CheckedBy {

        private int id;

        @SerializedName("full_name")
        private String fullName;

        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }
    }
}
