# app/schemas/ride.py

from typing import Optional, List
from pydantic import BaseModel


class CreateRideRequest(BaseModel):
    pickup_lat: float
    pickup_lng: float
    pickup_address: str

    dropoff_lat: float
    dropoff_lng: float
    dropoff_address: str


class UpdateRideStatusRequest(BaseModel):
    status: str
    note: Optional[str] = None


class CancelRideRequest(BaseModel):
    cancel_reason: Optional[str] = None


class RideResponse(BaseModel):
    id: int
    customer_id: int
    assigned_driver_id: Optional[int] = None

    pickup_lat: float
    pickup_lng: float
    pickup_address: str

    dropoff_lat: float
    dropoff_lng: float
    dropoff_address: str

    status: str
    estimated_fare: Optional[float] = None
    final_fare: Optional[float] = None
    cancel_reason: Optional[str] = None

    class Config:
        from_attributes = True


class RideListResponse(BaseModel):
    rides: List[RideResponse]