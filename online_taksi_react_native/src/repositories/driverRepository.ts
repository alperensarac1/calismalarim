import { ApiClient } from "../network/apiClient";
import {
    AvailableRideListResponse,
    DriverProfileResponse,
} from "../models/driverModels";
import { RideResponse } from "../models/rideModels";

export const DriverRepository = {
    setOnline(isOnline: boolean) {
        return ApiClient.put<DriverProfileResponse>("driver/online-status", {
            is_online: isOnline,
        });
    },

    updateLocation(lat: number, lng: number) {
        return ApiClient.put<DriverProfileResponse>("driver/location", {
            lat,
            lng,
        });
    },

    getAvailableRides() {
        return ApiClient.get<AvailableRideListResponse>("driver/available-rides");
    },

    acceptRide(rideId: number) {
        return ApiClient.put<RideResponse>(`driver/rides/${rideId}/accept`, {});
    },

    updateRideStatus(rideId: number, status: string, note?: string) {
        return ApiClient.put<RideResponse>(`driver/rides/${rideId}/status`, {
            status,
            note: note ?? null,
        });
    },
};