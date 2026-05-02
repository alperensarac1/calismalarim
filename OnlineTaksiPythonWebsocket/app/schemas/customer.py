# app/schemas/customer.py

from typing import List
from pydantic import BaseModel
from app.schemas.ride import RideResponse


class CustomerRideListResponse(BaseModel):
    rides: List[RideResponse]