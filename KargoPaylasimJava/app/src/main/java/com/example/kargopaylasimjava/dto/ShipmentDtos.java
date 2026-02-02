package com.example.kargopaylasimjava.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShipmentDtos {

    public static class ShipmentCreateReq {
        public String receiver_phone;
        public Integer sender_address_id; // nullable

        public ShipmentCreateReq(String receiver_phone, Integer sender_address_id) {
            this.receiver_phone = receiver_phone;
            this.sender_address_id = sender_address_id;
        }
    }

    public static class ShipmentCancelReq {
        public int shipment_id;
        public ShipmentCancelReq(int shipment_id) { this.shipment_id = shipment_id; }
    }

    public static class ShipmentRegenerateReq {
        public int shipment_id;
        public ShipmentRegenerateReq(int shipment_id) { this.shipment_id = shipment_id; }
    }

    public static class ShipmentDeleteReq {
        @SerializedName("id")
        public int id;
        public ShipmentDeleteReq(int id) { this.id = id; }
    }

    public static class ShipmentCreateResp {
        public int shipment_id;
        public String pickup_code;
        public String code_expires_at;
        public String status;
    }

    public static class ShipmentRegenerateResp {
        public int shipment_id;
        public String pickup_code;
        public String code_expires_at;
        public String status;
    }

    public static class ShipmentDeleteResp {
        public boolean deleted;
        public int id;
    }

    public static class ShipmentItemDto {
        public int id;
        public String pickup_code;
        public String status;
        public String code_expires_at;
        public String created_at;
        public String used_at; // nullable
    }

    public static class ShipmentListResp {
        @SerializedName("items")
        public List<ShipmentDto> shipments;
    }

    public static class ShipmentDto {
        public int id;

        @SerializedName("sender_user_id")
        public int senderUserId;

        @SerializedName("receiver_user_id")
        public int receiverUserId;

        @SerializedName("pickup_code")
        public String pickupCode;

        public String status;

        @SerializedName("code_expires_at")
        public String codeExpiresAt; // nullable

        @SerializedName("cargo_company_id")
        public Long cargoCompanyId; // nullable

        @SerializedName("cargo_company_name")
        public String cargoCompanyName; // nullable

        public String role;
        public Boolean visible; // nullable

        @SerializedName("sender_initials")
        public String senderInitials; // nullable

        @SerializedName("receiver_address_title")
        public String receiverAddressTitle; // nullable
    }

    public static class ShipmentDetailDto {
        public int id;

        @SerializedName("pickup_code")
        public String pickupCode;

        public String status;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;

        @SerializedName("code_expires_at")
        public String codeExpiresAtRaw;

        @SerializedName("used_at")
        public String usedAt;

        @SerializedName("confirmed_at")
        public String confirmedAt;

        @SerializedName("in_transit_at")
        public String inTransitAt;

        @SerializedName("delivered_at")
        public String deliveredAt;

        @SerializedName("cancelled_at")
        public String cancelledAt;

        @SerializedName("expired_at")
        public String expiredAt;

        @SerializedName("is_sender")
        public boolean isSender;

        @SerializedName("cargo_company_id")
        public Long cargoCompanyId;

        @SerializedName("cargo_company_name")
        public String cargoCompanyName;

        public String getCodeExpiresAt() { return codeExpiresAtRaw; }
    }

    public static class ShipmentDetailResp {
        public ShipmentDetailDto shipment;
    }
}
