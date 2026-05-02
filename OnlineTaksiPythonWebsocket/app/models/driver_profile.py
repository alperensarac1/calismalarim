# app/models/driver_profile.py

from datetime import datetime
from sqlalchemy import Column, Integer, ForeignKey, String, Boolean, Float, DateTime
from app.core.database import Base


class DriverProfile(Base):
    __tablename__ = "driver_profiles"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)

    plate_number = Column(String(20), nullable=True)
    car_brand = Column(String(50), nullable=True)
    car_model = Column(String(50), nullable=True)
    car_color = Column(String(30), nullable=True)
    driver_license_no = Column(String(50), nullable=True)

    is_verified = Column(Boolean, default=False)
    is_online = Column(Boolean, default=False)

    current_lat = Column(Float, nullable=True)
    current_lng = Column(Float, nullable=True)

    last_location_at = Column(DateTime, nullable=True)

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)