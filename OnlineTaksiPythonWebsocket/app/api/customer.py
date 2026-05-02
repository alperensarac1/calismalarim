# app/api/customer.py

from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.models.ride_request import RideRequest
from app.models.ride_status_log import RideStatusLog
from app.schemas.ride import CreateRideRequest, RideResponse, CancelRideRequest
from app.schemas.customer import CustomerRideListResponse
from app.utils.enums import UserRole, RideStatus
from app.websocket.connection_manager import manager
from app.websocket.events import WsEvent

router = APIRouter(prefix="/customer", tags=["Customer"])


@router.post("/rides", response_model=RideResponse)
async def create_ride(
    request: CreateRideRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.CUSTOMER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece müşteri hesabı ile yapılabilir.")

    active_statuses = [
        RideStatus.REQUESTED.value,
        RideStatus.SEARCHING_DRIVER.value,
        RideStatus.DRIVER_ASSIGNED.value,
        RideStatus.DRIVER_ARRIVING.value,
        RideStatus.DRIVER_ARRIVED.value,
        RideStatus.RIDE_STARTED.value
    ]

    active_ride = (
        db.query(RideRequest)
        .filter(
            RideRequest.customer_id == current_user.id,
            RideRequest.status.in_(active_statuses)
        )
        .first()
    )

    if active_ride:
        raise HTTPException(status_code=400, detail="Zaten aktif bir taksi çağrınız var.")

    estimated_fare = 120.0

    ride = RideRequest(
        customer_id=current_user.id,
        pickup_lat=request.pickup_lat,
        pickup_lng=request.pickup_lng,
        pickup_address=request.pickup_address,
        dropoff_lat=request.dropoff_lat,
        dropoff_lng=request.dropoff_lng,
        dropoff_address=request.dropoff_address,
        status=RideStatus.REQUESTED.value,
        estimated_fare=estimated_fare
    )

    db.add(ride)
    db.commit()
    db.refresh(ride)

    log = RideStatusLog(
        ride_request_id=ride.id,
        status=RideStatus.REQUESTED.value,
        note="Müşteri yeni taksi çağrısı oluşturdu.",
        created_by_user_id=current_user.id
    )
    db.add(log)
    db.commit()

    # Online driver'lara yeni ride bildir
    await manager.broadcast_to_online_drivers(
        event=WsEvent.NEW_RIDE_REQUEST,
        data={
            "ride_id": ride.id,
            "customer_id": ride.customer_id,
            "pickup_lat": ride.pickup_lat,
            "pickup_lng": ride.pickup_lng,
            "pickup_address": ride.pickup_address,
            "dropoff_lat": ride.dropoff_lat,
            "dropoff_lng": ride.dropoff_lng,
            "dropoff_address": ride.dropoff_address,
            "estimated_fare": ride.estimated_fare,
            "status": ride.status
        }
    )

    return ride


@router.get("/rides", response_model=CustomerRideListResponse)
def get_my_rides(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.CUSTOMER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece müşteri hesabı ile yapılabilir.")

    rides = (
        db.query(RideRequest)
        .filter(RideRequest.customer_id == current_user.id)
        .order_by(RideRequest.created_at.desc())
        .all()
    )

    return CustomerRideListResponse(rides=rides)


@router.put("/rides/{ride_id}/cancel", response_model=RideResponse)
async def cancel_my_ride(
    ride_id: int,
    request: CancelRideRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.role != UserRole.CUSTOMER.value:
        raise HTTPException(status_code=403, detail="Bu işlem sadece müşteri hesabı ile yapılabilir.")

    ride = (
        db.query(RideRequest)
        .filter(
            RideRequest.id == ride_id,
            RideRequest.customer_id == current_user.id
        )
        .first()
    )

    if not ride:
        raise HTTPException(status_code=404, detail="Ride bulunamadı.")

    cancellable_statuses = [
        RideStatus.REQUESTED.value,
        RideStatus.SEARCHING_DRIVER.value,
        RideStatus.DRIVER_ASSIGNED.value,
        RideStatus.DRIVER_ARRIVING.value,
        RideStatus.DRIVER_ARRIVED.value
    ]

    if ride.status not in cancellable_statuses:
        raise HTTPException(status_code=400, detail="Bu ride bu aşamada müşteri tarafından iptal edilemez.")

    previous_driver_id = ride.assigned_driver_id

    ride.status = RideStatus.CANCELLED_BY_CUSTOMER.value
    ride.cancel_reason = request.cancel_reason or "Müşteri iptal etti."
    ride.cancelled_at = datetime.utcnow()

    db.commit()
    db.refresh(ride)

    log = RideStatusLog(
        ride_request_id=ride.id,
        status=RideStatus.CANCELLED_BY_CUSTOMER.value,
        note=ride.cancel_reason,
        created_by_user_id=current_user.id
    )
    db.add(log)
    db.commit()

    # Müşteriye bilgi geç
    await manager.send_to_user(
        user_id=current_user.id,
        event=WsEvent.RIDE_CANCELLED,
        data={
            "ride_id": ride.id,
            "status": ride.status,
            "cancel_reason": ride.cancel_reason
        }
    )

    # Şoför atanmışsa ona da bildir
    if previous_driver_id:
        await manager.send_to_user(
            user_id=previous_driver_id,
            event=WsEvent.RIDE_CANCELLED,
            data={
                "ride_id": ride.id,
                "status": ride.status,
                "cancel_reason": ride.cancel_reason
            }
        )

    return ride