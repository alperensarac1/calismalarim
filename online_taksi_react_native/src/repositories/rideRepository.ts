import { ApiClient } from "../network/apiClient";
import { CreateRideRequest, RideResponse } from "../models/rideModels";

export const RideRepository = {
    createRide(body: CreateRideRequest) {
        return ApiClient.post<RideResponse>("customer/rides", body);
    },
};