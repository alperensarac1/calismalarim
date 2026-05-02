# app/models/ride_request.py

from datetime import datetime
from sqlalchemy import Column, Integer, ForeignKey, String, Float, DateTime
from app.core.database import Base


class RideRequest(Base):
    __tablename__ = "ride_requests"

    id = Column(Integer, primary_key=True, index=True)

    customer_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    assigned_driver_id = Column(Integer, ForeignKey("users.id"), nullable=True, index=True)

    pickup_lat = Column(Float, nullable=False)
    pickup_lng = Column(Float, nullable=False)
    pickup_address = Column(String(255), nullable=False)

    dropoff_lat = Column(Float, nullable=False)
    dropoff_lng = Column(Float, nullable=False)
    dropoff_address = Column(String(255), nullable=False)

    status = Column(String(50), nullable=False, default="REQUESTED", index=True)

    estimated_fare = Column(Float, nullable=True)
    final_fare = Column(Float, nullable=True)

    cancel_reason = Column(String(255), nullable=True)

    requested_at = Column(DateTime, default=datetime.utcnow)
    accepted_at = Column(DateTime, nullable=True)
    started_at = Column(DateTime, nullable=True)
    completed_at = Column(DateTime, nullable=True)
    cancelled_at = Column(DateTime, nullable=True)

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)