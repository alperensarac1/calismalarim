# app/models/ride_status_log.py

from datetime import datetime
from sqlalchemy import Column, Integer, ForeignKey, String, DateTime
from app.core.database import Base


class RideStatusLog(Base):
    __tablename__ = "ride_status_logs"

    id = Column(Integer, primary_key=True, index=True)

    ride_request_id = Column(Integer, ForeignKey("ride_requests.id"), nullable=False, index=True)
    status = Column(String(50), nullable=False)
    note = Column(String(255), nullable=True)
    created_by_user_id = Column(Integer, ForeignKey("users.id"), nullable=True)

    created_at = Column(DateTime, default=datetime.utcnow)