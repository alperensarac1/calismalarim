export type CreateRideRequest = {
    pickup_lat: number;
    pickup_lng: number;
    pickup_address: string;
    dropoff_lat: number;
    dropoff_lng: number;
    dropoff_address: string;
};

export type RideResponse = {
    id: number;
    customer_id: number;
    assigned_driver_id?: number | null;
    pickup_lat: number;
    pickup_lng: number;
    pickup_address: string;
    dropoff_lat: number;
    dropoff_lng: number;
    dropoff_address: string;
    status: string;
    estimated_fare?: number | null;
    final_fare?: number | null;
    cancel_reason?: string | null;
};