from __future__ import annotations

from datetime import datetime
from typing import Literal

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Query,
    status,
)
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import (
    AuthenticationMethod,
    AuthenticationResult,
    DevicePlatform,
)
from app.routers.challenges import (
    AuthenticatedBackendUserContext,
    get_authenticated_backend_user,
)
from app.schemas import (
    ApiResponse,
    AuthenticationLogListResponse,
    AuthenticationLogResponse,
    PaginationMetadata,
)
from app.services.authentication_log_service import (
    AuthenticationLogNotFoundError,
    AuthenticationLogServiceError,
    InvalidAuthenticationLogDateRangeError,
    authentication_log_service,
)


# =========================================================
# ROUTER TANIMI
# =========================================================


router = APIRouter(
    prefix="/api/admin/authentication-logs",
    tags=["Admin Authentication Logs"],
)


# =========================================================
# ADMIN YETKİ KONTROLÜ
# =========================================================


def ensure_admin_role(
    context: AuthenticatedBackendUserContext,
) -> None:
    """
    Mevcut backend tarafından doğrulanmış kullanıcının
    Admin rolüne sahip olup olmadığını kontrol eder.

    Rol bilgisi Python request gövdesinden alınmaz.
    Mevcut .NET backend'in /api/Auth/me cevabından gelir.
    """

    role = context.user.role

    normalized_role = (
        role.strip().lower()
        if role is not None
        else ""
    )

    if normalized_role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Bu işlem yalnızca Admin rolüne sahip "
                "kullanıcılar tarafından yapılabilir."
            ),
        )


def get_authenticated_admin(
    context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_backend_user,
    ),
) -> AuthenticatedBackendUserContext:
    """
    Backend access tokenını doğrular ve kullanıcının
    Admin rolüne sahip olmasını zorunlu tutar.
    """

    ensure_admin_role(
        context,
    )

    return context


# =========================================================
# RESPONSE DÖNÜŞÜMLERİ
# =========================================================


def map_authentication_log_response(
    authentication_log,
) -> AuthenticationLogResponse:
    """
    SQLAlchemy AuthenticationLog modelini güvenli
    Pydantic response modeline dönüştürür.
    """

    return AuthenticationLogResponse.model_validate(
        authentication_log,
    )


# =========================================================
# LOG LİSTELEME
# =========================================================


@router.get(
    "",
    response_model=ApiResponse,
    summary="Güvenlik loglarını filtreli olarak listeler",
)
def get_authentication_logs(
    page: int = Query(
        default=1,
        ge=1,
        description="Sayfa numarası",
    ),
    page_size: int = Query(
        default=20,
        ge=1,
        le=100,
        description="Bir sayfadaki kayıt sayısı",
    ),
    external_user_id: str | None = Query(
        default=None,
        max_length=150,
        description="Ana backend kullanıcı ID filtresi",
    ),
    email: str | None = Query(
        default=None,
        max_length=320,
        description="E-posta filtresi",
    ),
    result: AuthenticationResult | None = Query(
        default=None,
        description="Doğrulama sonucu filtresi",
    ),
    method: AuthenticationMethod | None = Query(
        default=None,
        description="Doğrulama yöntemi filtresi",
    ),
    platform: DevicePlatform | None = Query(
        default=None,
        description="Cihaz platformu filtresi",
    ),
    risk_level: Literal[
        "low",
        "medium",
        "high",
        "critical",
    ] | None = Query(
        default=None,
        description="Risk seviyesi filtresi",
    ),
    request_ip: str | None = Query(
        default=None,
        max_length=64,
        description="İstek IP adresi filtresi",
    ),
    has_location: bool | None = Query(
        default=None,
        description=(
            "GPS koordinatı bulunan veya bulunmayan "
            "kayıtları filtreler"
        ),
    ),
    location_mismatch: bool | None = Query(
        default=None,
        description=(
            "GPS ve IP konumu uyuşmazlık filtresi"
        ),
    ),
    city: str | None = Query(
        default=None,
        max_length=150,
        description="Şehir veya ilçe filtresi",
    ),
    country_code: str | None = Query(
        default=None,
        min_length=2,
        max_length=10,
        description="Ülke kodu filtresi",
    ),
    search: str | None = Query(
        default=None,
        max_length=200,
        description=(
            "Kullanıcı, e-posta, cihaz, IP veya "
            "konum alanlarında genel arama"
        ),
    ),
    start_date: datetime | None = Query(
        default=None,
        description="Başlangıç tarihi",
    ),
    end_date: datetime | None = Query(
        default=None,
        description="Bitiş tarihi",
    ),
    _admin_context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_admin,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    AuthenticationLog kayıtlarını filtreli ve sayfalı
    şekilde döndürür.

    En yeni güvenlik kayıtları önce listelenir.

    Authorization başlığında mevcut .NET backend
    access tokenı gönderilmelidir:

    Authorization: Bearer <backend-access-token>
    """

    try:
        result_data = (
            authentication_log_service.get_logs(
                db,
                page=page,
                page_size=page_size,
                external_user_id=external_user_id,
                email=email,
                result=result,
                method=method,
                platform=platform,
                risk_level=risk_level,
                request_ip=request_ip,
                has_location=has_location,
                location_mismatch=location_mismatch,
                city=city,
                country_code=country_code,
                search=search,
                start_date=start_date,
                end_date=end_date,
            )
        )

    except InvalidAuthenticationLogDateRangeError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except AuthenticationLogServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    response_items = [
        map_authentication_log_response(
            authentication_log,
        )
        for authentication_log in result_data.items
    ]

    response_data = AuthenticationLogListResponse(
        items=response_items,
        pagination=PaginationMetadata(
            page=result_data.page,
            page_size=result_data.page_size,
            total_count=result_data.total_count,
            total_pages=result_data.total_pages,
            has_previous_page=(
                result_data.has_previous_page
            ),
            has_next_page=(
                result_data.has_next_page
            ),
        ),
    )

    return ApiResponse(
        success=True,
        message=(
            "Güvenlik logları başarıyla getirildi."
        ),
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )


# =========================================================
# LOG ÖZETİ
# =========================================================


@router.get(
    "/summary",
    response_model=ApiResponse,
    summary="Güvenlik logu özet istatistiklerini getirir",
)
def get_authentication_log_summary(
    start_date: datetime | None = Query(
        default=None,
        description="Başlangıç tarihi",
    ),
    end_date: datetime | None = Query(
        default=None,
        description="Bitiş tarihi",
    ),
    _admin_context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_admin,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Admin ekranında kullanılabilecek güvenlik
    istatistiklerini döndürür.

    Dönen bilgiler:

    - Toplam doğrulama kaydı
    - Başarılı kayıt sayısı
    - Başarısız kayıt sayısı
    - Kullanıcı tarafından reddedilen kayıt sayısı
    - Risk seviyelerine göre kayıt sayıları
    - Konum uyuşmazlığı bulunan kayıt sayısı
    """

    try:
        summary = (
            authentication_log_service.get_summary(
                db,
                start_date=start_date,
                end_date=end_date,
            )
        )

    except InvalidAuthenticationLogDateRangeError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except AuthenticationLogServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    return ApiResponse(
        success=True,
        message=(
            "Güvenlik logu özeti başarıyla getirildi."
        ),
        data={
            "total_count": summary.total_count,
            "success_count": summary.success_count,
            "failed_count": summary.failed_count,
            "rejected_count": summary.rejected_count,
            "low_risk_count": summary.low_risk_count,
            "medium_risk_count": (
                summary.medium_risk_count
            ),
            "high_risk_count": (
                summary.high_risk_count
            ),
            "critical_risk_count": (
                summary.critical_risk_count
            ),
            "location_mismatch_count": (
                summary.location_mismatch_count
            ),
        },
        errors={},
    )


# =========================================================
# TEK LOG DETAYI
# =========================================================


@router.get(
    "/{log_public_id}",
    response_model=ApiResponse,
    summary="Tek bir güvenlik logunun detayını getirir",
)
def get_authentication_log_by_public_id(
    log_public_id: str,
    _admin_context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_admin,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Public ID değerine göre tek bir güvenlik logunu
    döndürür.
    """

    try:
        authentication_log = (
            authentication_log_service
            .get_log_by_public_id(
                db,
                log_public_id=log_public_id,
            )
        )

    except AuthenticationLogNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except AuthenticationLogServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    response_data = map_authentication_log_response(
        authentication_log,
    )

    return ApiResponse(
        success=True,
        message=(
            "Güvenlik logu başarıyla getirildi."
        ),
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )