export type DriverProfileResponse = {
    user_id: number;
    is_online: boolean;
    current_lat?: number | null;
    current_lng?: number | null;
};

export type AvailableRideItem = {
    id: number;
    customer_id: number;
    pickup_lat: number;
    pickup_lng: number;
    pickup_address: string;
    dropoff_lat: number;
    dropoff_lng: number;
    dropoff_address: string;
    status: string;
    estimated_fare?: number | null;
};

export type AvailableRideListResponse = {
    rides: AvailableRideItem[];
};