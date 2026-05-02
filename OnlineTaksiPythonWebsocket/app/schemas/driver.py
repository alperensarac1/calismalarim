# app/schemas/driver.py

from typing import Optional, List
from pydantic import BaseModel


class DriverOnlineStatusRequest(BaseModel):
    is_online: bool


class DriverLocationUpdateRequest(BaseModel):
    lat: float
    lng: float


class DriverProfileResponse(BaseModel):
    user_id: int
    is_online: bool
    current_lat: Optional[float] = None
    current_lng: Optional[float] = None

    class Config:
        from_attributes = True


class AvailableRideItem(BaseModel):
    id: int
    customer_id: int
    pickup_lat: float
    pickup_lng: float
    pickup_address: str
    dropoff_lat: float
    dropoff_lng: float
    dropoff_address: str
    status: str
    estimated_fare: Optional[float] = None

    class Config:
        from_attributes = True


class AvailableRideListResponse(BaseModel):
    rides: List[AvailableRideItem]