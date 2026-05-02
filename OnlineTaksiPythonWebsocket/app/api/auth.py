# app/api/auth.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.security import hash_password, verify_password, create_access_token
from app.models.user import User
from app.models.customer_profile import CustomerProfile
from app.models.driver_profile import DriverProfile
from app.schemas.auth import RegisterRequest, LoginRequest, AuthResponse
from app.utils.enums import UserRole

router = APIRouter(prefix="/auth", tags=["Auth"])


@router.post("/register", response_model=AuthResponse)
def register(request: RegisterRequest, db: Session = Depends(get_db)):
    # Aynı telefon numarası var mı?
    existing_phone = db.query(User).filter(User.phone == request.phone).first()
    if existing_phone:
        raise HTTPException(status_code=400, detail="Bu telefon numarası zaten kayıtlı.")

    # Email verilmişse onu da kontrol et
    if request.email:
        existing_email = db.query(User).filter(User.email == request.email).first()
        if existing_email:
            raise HTTPException(status_code=400, detail="Bu email zaten kayıtlı.")

    # Rol doğrulama
    if request.role not in [UserRole.CUSTOMER.value, UserRole.DRIVER.value]:
        raise HTTPException(status_code=400, detail="Geçersiz kullanıcı rolü.")

    # Kullanıcıyı oluştur
    user = User(
        full_name=request.full_name,
        phone=request.phone,
        email=request.email,
        password_hash=hash_password(request.password),
        role=request.role
    )

    db.add(user)
    db.commit()
    db.refresh(user)

    # Role göre otomatik profil oluştur
    if user.role == UserRole.CUSTOMER.value:
        customer_profile = CustomerProfile(
            user_id=user.id,
            default_payment_type="cash"
        )
        db.add(customer_profile)

    elif user.role == UserRole.DRIVER.value:
        driver_profile = DriverProfile(
            user_id=user.id,
            is_verified=False,
            is_online=False
        )
        db.add(driver_profile)

    db.commit()

    # JWT token üret
    token = create_access_token({
        "user_id": user.id,
        "role": user.role
    })

    return AuthResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        full_name=user.full_name,
        role=user.role
    )


@router.post("/login", response_model=AuthResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.phone == request.phone).first()

    if not user:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı.")

    if not verify_password(request.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Şifre hatalı.")

    token = create_access_token({
        "user_id": user.id,
        "role": user.role
    })

    return AuthResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        full_name=user.full_name,
        role=user.role
    )