# app/api/driver.py

from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.models.driver_profile import DriverProfile
from app.models.ride_request import RideRequest
from app.models.ride_status_log import RideStatusLog
from app.schemas.driver import (
    DriverOnlineStatusRequest,
    DriverLocationUpdateRequest,
    DriverProfileResponse,
    AvailableRideListResponse
)
from app.schemas.ride import RideResponse, RideListResponse, UpdateRideStatusRequest
from app.utils.enums import UserRole, RideStatus
from app.websocket.connection_manager import manager
from app.websocket.events import WsEvent

router = APIRouter(prefix="/driver", tags=["Driver"])


@router.get("/me", response_model=DriverProfileResponse)
def get_driver_me(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    driver_profile = db.query(DriverProfile).filter(DriverProfile.user_id == current_user.id).first()
    if not driver_profile:
        raise HTTPException(status_code=404, detail="Şoför profili bulunamadı.")

    return DriverProfileResponse(
        user_id=current_user.id,
        is_online=driver_profile.is_online,
        current_lat=driver_profile.current_lat,
        current_lng=driver_profile.current_lng
    )


@router.put("/online-status", response_model=DriverProfileResponse)
def update_online_status(
    request: DriverOnlineStatusRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    driver_profile = db.query(DriverProfile).filter(DriverProfile.user_id == current_user.id).first()
    if not driver_profile:
        raise HTTPException(status_code=404, detail="Şoför profili bulunamadı.")

    driver_profile.is_online = request.is_online
    db.commit()
    db.refresh(driver_profile)

    return DriverProfileResponse(
        user_id=current_user.id,
        is_online=driver_profile.is_online,
        current_lat=driver_profile.current_lat,
        current_lng=driver_profile.current_lng
    )


@router.put("/location", response_model=DriverProfileResponse)
async def update_location(
    request: DriverLocationUpdateRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    driver_profile = db.query(DriverProfile).filter(DriverProfile.user_id == current_user.id).first()
    if not driver_profile:
        raise HTTPException(status_code=404, detail="Şoför profili bulunamadı.")

    driver_profile.current_lat = request.lat
    driver_profile.current_lng = request.lng
    driver_profile.last_location_at = datetime.utcnow()

    db.commit()
    db.refresh(driver_profile)

    # Şoförün aktif ride'ı varsa müşteriye canlı konum gönder
    active_ride = (
        db.query(RideRequest)
        .filter(
            RideRequest.assigned_driver_id == current_user.id,
            RideRequest.status.in_([
                RideStatus.DRIVER_ASSIGNED.value,
                RideStatus.DRIVER_ARRIVING.value,
                RideStatus.DRIVER_ARRIVED.value,
                RideStatus.RIDE_STARTED.value
            ])
        )
        .first()
    )

    if active_ride:
        await manager.send_to_user(
            user_id=active_ride.customer_id,
            event=WsEvent.DRIVER_LOCATION,
            data={
                "ride_id": active_ride.id,
                "driver_id": current_user.id,
                "lat": driver_profile.current_lat,
                "lng": driver_profile.current_lng
            }
        )

    return DriverProfileResponse(
        user_id=current_user.id,
        is_online=driver_profile.is_online,
        current_lat=driver_profile.current_lat,
        current_lng=driver_profile.current_lng
    )


@router.get("/available-rides", response_model=AvailableRideListResponse)
def get_available_rides(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    driver_profile = db.query(DriverProfile).filter(DriverProfile.user_id == current_user.id).first()
    if not driver_profile:
        raise HTTPException(status_code=404, detail="Şoför profili bulunamadı.")

    if not driver_profile.is_online:
        raise HTTPException(status_code=400, detail="Açık ride'ları görmek için önce online olmalısınız.")

    rides = (
        db.query(RideRequest)
        .filter(RideRequest.status == RideStatus.REQUESTED.value)
        .order_by(RideRequest.requested_at.desc())
        .all()
    )

    return AvailableRideListResponse(rides=rides)


@router.put("/rides/{ride_id}/accept", response_model=RideResponse)
async def accept_ride(
    ride_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    driver_profile = db.query(DriverProfile).filter(DriverProfile.user_id == current_user.id).first()
    if not driver_profile:
        raise HTTPException(status_code=404, detail="Şoför profili bulunamadı.")

    if not driver_profile.is_online:
        raise HTTPException(status_code=400, detail="Ride kabul etmek için önce online olmalısınız.")

    active_driver_ride = (
        db.query(RideRequest)
        .filter(
            RideRequest.assigned_driver_id == current_user.id,
            RideRequest.status.in_([
                RideStatus.DRIVER_ASSIGNED.value,
                RideStatus.DRIVER_ARRIVING.value,
                RideStatus.DRIVER_ARRIVED.value,
                RideStatus.RIDE_STARTED.value
            ])
        )
        .first()
    )

    if active_driver_ride:
        raise HTTPException(status_code=400, detail="Zaten aktif olarak yürüttüğünüz bir ride var.")

    ride = db.query(RideRequest).filter(RideRequest.id == ride_id).first()
    if not ride:
        raise HTTPException(status_code=404, detail="Ride bulunamadı.")

    if ride.status != RideStatus.REQUESTED.value:
        raise HTTPException(status_code=400, detail="Bu ride artık müsait değil.")

    ride.assigned_driver_id = current_user.id
    ride.status = RideStatus.DRIVER_ASSIGNED.value
    ride.accepted_at = datetime.utcnow()

    db.commit()
    db.refresh(ride)

    log = RideStatusLog(
        ride_request_id=ride.id,
        status=RideStatus.DRIVER_ASSIGNED.value,
        note="Ride şoför tarafından kabul edildi.",
        created_by_user_id=current_user.id
    )
    db.add(log)
    db.commit()

    # Müşteriye canlı kabul bildirimi
    await manager.send_to_user(
        user_id=ride.customer_id,
        event=WsEvent.RIDE_ACCEPTED,
        data={
            "ride_id": ride.id,
            "driver_id": current_user.id,
            "driver_name": current_user.full_name,
            "status": ride.status
        }
    )

    return ride


@router.put("/rides/{ride_id}/reject")
def reject_ride(
    ride_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    ride = db.query(RideRequest).filter(RideRequest.id == ride_id).first()
    if not ride:
        raise HTTPException(status_code=404, detail="Ride bulunamadı.")

    if ride.status != RideStatus.REQUESTED.value:
        raise HTTPException(status_code=400, detail="Bu ride artık açık durumda değil.")

    log = RideStatusLog(
        ride_request_id=ride.id,
        status="REJECTED_BY_DRIVER",
        note="Şoför ride teklifini reddetti.",
        created_by_user_id=current_user.id
    )
    db.add(log)
    db.commit()

    return {"message": "Ride reddedildi."}


@router.get("/rides/my-active", response_model=RideListResponse)
def get_my_active_rides(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    rides = (
        db.query(RideRequest)
        .filter(
            RideRequest.assigned_driver_id == current_user.id,
            RideRequest.status.in_([
                RideStatus.DRIVER_ASSIGNED.value,
                RideStatus.DRIVER_ARRIVING.value,
                RideStatus.DRIVER_ARRIVED.value,
                RideStatus.RIDE_STARTED.value
            ])
        )
        .order_by(RideRequest.updated_at.desc())
        .all()
    )

    return RideListResponse(rides=rides)


@router.put("/rides/{ride_id}/status", response_model=RideResponse)
async def update_ride_status(
    ride_id: int,
    request: UpdateRideStatusRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.DRIVER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece şoför hesabı ile yapılabilir.")

    ride = (
        db.query(RideRequest)
        .filter(
            RideRequest.id == ride_id,
            RideRequest.assigned_driver_id == current_user.id
        )
        .first()
    )

    if not ride:
        raise HTTPException(status_code=404, detail="Size atanmış ride bulunamadı.")

    allowed_next_statuses = [
        RideStatus.DRIVER_ARRIVING.value,
        RideStatus.DRIVER_ARRIVED.value,
        RideStatus.RIDE_STARTED.value,
        RideStatus.RIDE_COMPLETED.value,
        RideStatus.CANCELLED_BY_DRIVER.value
    ]

    if request.status not in allowed_next_statuses:
        raise HTTPException(status_code=400, detail="Geçersiz ride status değeri.")

    current = ride.status
    new_status = request.status

    valid_transitions = {
        RideStatus.DRIVER_ASSIGNED.value: [
            RideStatus.DRIVER_ARRIVING.value,
            RideStatus.CANCELLED_BY_DRIVER.value
        ],
        RideStatus.DRIVER_ARRIVING.value: [
            RideStatus.DRIVER_ARRIVED.value,
            RideStatus.CANCELLED_BY_DRIVER.value
        ],
        RideStatus.DRIVER_ARRIVED.value: [
            RideStatus.RIDE_STARTED.value,
            RideStatus.CANCELLED_BY_DRIVER.value
        ],
        RideStatus.RIDE_STARTED.value: [
            RideStatus.RIDE_COMPLETED.value,
            RideStatus.CANCELLED_BY_DRIVER.value
        ]
    }

    if current not in valid_transitions or new_status not in valid_transitions[current]:
        raise HTTPException(
            status_code=400,
            detail=f"Geçersiz durum geçişi. Mevcut: {current}, yeni: {new_status}"
        )

    ride.status = new_status

    if new_status == RideStatus.RIDE_STARTED.value:
        ride.started_at = datetime.utcnow()

    elif new_status == RideStatus.RIDE_COMPLETED.value:
        ride.completed_at = datetime.utcnow()
        ride.final_fare = ride.estimated_fare or 120.0

    elif new_status == RideStatus.CANCELLED_BY_DRIVER.value:
        ride.cancelled_at = datetime.utcnow()
        ride.cancel_reason = request.note or "Şoför iptal etti."

    db.commit()
    db.refresh(ride)

    log = RideStatusLog(
        ride_request_id=ride.id,
        status=new_status,
        note=request.note,
        created_by_user_id=current_user.id
    )
    db.add(log)
    db.commit()

    # Müşteriye canlı durum bildir
    event_name = WsEvent.RIDE_CANCELLED if new_status == RideStatus.CANCELLED_BY_DRIVER.value else WsEvent.RIDE_STATUS_CHANGED

    await manager.send_to_user(
        user_id=ride.customer_id,
        event=event_name,
        data={
            "ride_id": ride.id,
            "driver_id": current_user.id,
            "status": ride.status,
            "note": request.note,
            "final_fare": ride.final_fare
        }
    )

    return ride