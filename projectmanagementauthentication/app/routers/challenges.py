from __future__ import annotations

from dataclasses import dataclass

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Request,
    status,
)
from fastapi.security import (
    HTTPAuthorizationCredentials,
    HTTPBearer,
)
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import (
    ApiResponse,
    CancelChallengeRequest,
    ChallengeResponse,
    ChallengeStatusResponse,
    CreateChallengeRequest,
    CreateChallengeResponse,
)
from app.services.backend_identity_service import (
    BackendIdentityError,
    BackendUnavailableError,
    InvalidBackendTokenError,
    VerifiedBackendUser,
    backend_identity_service,
)
from app.services.challenge_service import (
    ChallengeAlreadyCompletedError,
    ChallengeDeliveryError,
    ChallengeDeviceNotFoundError,
    ChallengeNotFoundError,
    ChallengeServiceError,
    ChallengeUserNotFoundError,
    challenge_service,
)


# =========================================================
# ROUTER
# =========================================================


router = APIRouter(
    prefix="/api/challenges",
    tags=["Authentication Challenges"],
)


# Authorization başlığındaki Bearer tokenını okumak için
# kullanılan FastAPI güvenlik şemasıdır.
backend_bearer_scheme = HTTPBearer(
    auto_error=False,
)


# =========================================================
# AUTHENTICATED BACKEND USER CONTEXT
# =========================================================


@dataclass(frozen=True, slots=True)
class AuthenticatedBackendUserContext:
    """
    Mevcut .NET backend tarafından doğrulanmış
    kullanıcı context nesnesidir.

    access_token:
        React uygulamasının .NET API'den aldığı JWT.

    user:
        /api/Auth/me endpointinden doğrulanmış kullanıcı.
    """

    access_token: str

    user: VerifiedBackendUser


# =========================================================
# REQUEST YARDIMCILARI
# =========================================================


def get_request_ip(
    request: Request,
) -> str | None:
    """
    HTTP isteğinin kaynak IP adresini döndürür.

    X-Forwarded-For yalnızca güvenilir reverse proxy
    kullanıldığında dikkate alınmalıdır.
    """

    forwarded_for = request.headers.get(
        "x-forwarded-for",
    )

    if forwarded_for:
        first_ip = (
            forwarded_for
            .split(",")[0]
            .strip()
        )

        if first_ip:
            return first_ip

    real_ip = request.headers.get(
        "x-real-ip",
    )

    if real_ip:
        normalized_ip = real_ip.strip()

        if normalized_ip:
            return normalized_ip

    if request.client is None:
        return None

    return request.client.host


def get_forwarded_ip(
    request: Request,
) -> str | None:
    """
    X-Forwarded-For başlığının ham değerini döndürür.

    Bu değer güvenlik loglarında saklanabilir.
    """

    value = request.headers.get(
        "x-forwarded-for",
    )

    if value is None:
        return None

    normalized_value = value.strip()

    return normalized_value or None


# =========================================================
# BACKEND TOKEN DOĞRULAMA
# =========================================================


def get_backend_access_token(
    credentials: (
        HTTPAuthorizationCredentials | None
    ) = Depends(
        backend_bearer_scheme,
    ),
) -> str:
    """
    Authorization başlığındaki mevcut .NET backend
    access tokenını alır.

    Beklenen başlık:

    Authorization: Bearer <access-token>
    """

    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Backend access tokenı gönderilmedi."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    if credentials.scheme.lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Authorization yöntemi Bearer olmalıdır."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    access_token = (
        credentials.credentials.strip()
    )

    if not access_token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Backend access tokenı boş olamaz."
            ),
        )

    return access_token


async def get_authenticated_backend_user(
    access_token: str = Depends(
        get_backend_access_token,
    ),
) -> AuthenticatedBackendUserContext:
    """
    Gelen access tokenı mevcut backend'in
    /api/Auth/me endpointi üzerinden doğrular.
    """

    try:
        verified_user = await (
            backend_identity_service
            .verify_access_token(
                access_token,
            )
        )

    except InvalidBackendTokenError as exception:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(
                exception,
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        ) from exception

    except BackendUnavailableError as exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=str(
                exception,
            ),
        ) from exception

    except BackendIdentityError as exception:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(
                exception,
            ),
        ) from exception

    return AuthenticatedBackendUserContext(
        access_token=access_token,
        user=verified_user,
    )


# =========================================================
# RESPONSE DÖNÜŞÜMLERİ
# =========================================================


def map_challenge_response(
    *,
    challenge,
    delivered_to_device: bool,
) -> ChallengeResponse:
    """
    SQLAlchemy challenge modelini güvenli API
    response modeline dönüştürür.

    code_hash ve nonce gibi güvenlik alanları
    response içerisine eklenmez.

    target_device None olabilir. Bu durum cihazsız
    demo challenge akışında normaldir.
    """

    target_device = challenge.target_device

    return ChallengeResponse(
        public_id=challenge.public_id,
        method=challenge.method,
        status=challenge.status,
        target_device_public_id=(
            target_device.public_id
            if target_device is not None
            else None
        ),
        target_device_name=(
            target_device.device_name
            if target_device is not None
            else None
        ),
        attempt_count=challenge.attempt_count,
        max_attempts=challenge.max_attempts,
        created_at=challenge.created_at,
        expires_at=challenge.expires_at,
        completed_at=challenge.completed_at,
        delivered_to_device=delivered_to_device,
    )


def ensure_challenge_owner(
    *,
    challenge,
    external_user_id: str,
) -> None:
    """
    Challenge'ın doğrulanmış kullanıcıya ait olduğunu
    kontrol eder.
    """

    challenge_user_id = (
        challenge.user.external_user_id
    )

    if challenge_user_id != external_user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Bu doğrulama isteğine erişim "
                "yetkiniz bulunmuyor."
            ),
        )


# =========================================================
# CHALLENGE OLUŞTURMA
# =========================================================


@router.post(
    "",
    response_model=ApiResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Yeni doğrulama challenge'ı oluşturur",
)
async def create_challenge(
    request_body: CreateChallengeRequest,
    request: Request,
    context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_backend_user,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Mevcut backend tarafından doğrulanmış kullanıcı için
    yeni Authenticator doğrulama isteği oluşturur.

    Normal akış:

    1. Kullanıcı doğrulanır.
    2. Kayıtlı cihaz bulunur.
    3. Challenge WebSocket ile mobil cihaza gönderilir.

    Demo akışı:

    1. Kullanıcı doğrulanır.
    2. Authenticator kullanıcısı yoksa otomatik oluşturulur.
    3. Kayıtlı cihaz olmasa bile challenge oluşturulur.
    4. delivered_to_device false döner.
    5. React doğrudan kod alanını gösterir.
    6. Kullanıcı 987456 test koduyla devam eder.
    """

    request_ip = get_request_ip(
        request,
    )

    forwarded_ip = get_forwarded_ip(
        request,
    )

    user_agent = request.headers.get(
        "user-agent",
    )

    try:
        result = await (
            challenge_service.create_challenge(
                db,

                # .NET backend kullanıcısının benzersiz ID'si.
                external_user_id=(
                    context.user.external_user_id
                ),

                # Kullanıcı Authenticator veritabanında
                # yoksa ExternalUser oluşturmak için
                # kullanılacak doğrulanmış bilgiler.
                email=context.user.email,

                display_name=(
                    context.user.display_name
                ),

                is_active=context.user.is_active,

                method=request_body.method,

                target_device_public_id=(
                    request_body
                    .target_device_public_id
                ),

                request_ip=request_ip,

                forwarded_ip=forwarded_ip,

                user_agent=user_agent,

                request_origin=(
                    request_body.request_origin
                ),

                request_correlation_id=(
                    request_body
                    .request_correlation_id
                ),
            )
        )

    except ChallengeUserNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeDeviceNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeDeliveryError as exception:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception


    challenge_response = map_challenge_response(
        challenge=result.challenge,
        delivered_to_device=(
            result.delivered_to_device
        ),
    )


    response_data = CreateChallengeResponse(
        challenge=challenge_response,

        expires_in_seconds=(
            result.expires_in_seconds
        ),

        polling_interval_seconds=2,
    )


    if result.delivered_to_device:
        response_message = (
            "Doğrulama isteği Authenticator "
            "cihazına gönderildi."
        )
    else:
        response_message = (
            "Demo doğrulama isteği oluşturuldu. "
            "Mobil cihaz bağlı değil. Test koduyla "
            "doğrulamaya devam edebilirsiniz."
        )


    return ApiResponse(
        success=True,
        message=response_message,
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )


# =========================================================
# CHALLENGE DURUMU
# =========================================================


@router.get(
    "/{challenge_public_id}/status",
    response_model=ApiResponse,
    summary="Challenge durumunu getirir",
)
def get_challenge_status(
    challenge_public_id: str,
    context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_backend_user,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Challenge durumunu döndürür.

    React uygulaması doğrulama tamamlanana kadar bu
    endpointi belirli aralıklarla çağırabilir.
    """

    try:
        result = (
            challenge_service
            .get_challenge_status(
                db,
                challenge_public_id=(
                    challenge_public_id
                ),
            )
        )

    except ChallengeNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception


    ensure_challenge_owner(
        challenge=result.challenge,
        external_user_id=(
            context.user.external_user_id
        ),
    )


    response_data = ChallengeStatusResponse(
        challenge_public_id=(
            result.challenge.public_id
        ),

        status=result.challenge.status,

        method=result.challenge.method,

        is_completed=result.is_completed,

        is_successful=result.is_successful,

        attempt_count=(
            result.challenge.attempt_count
        ),

        max_attempts=(
            result.challenge.max_attempts
        ),

        expires_at=result.challenge.expires_at,

        completed_at=(
            result.challenge.completed_at
        ),

        failure_reason=result.failure_reason,
    )


    return ApiResponse(
        success=True,
        message=(
            "Doğrulama isteği durumu "
            "başarıyla getirildi."
        ),
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )


# =========================================================
# CHALLENGE İPTALİ
# =========================================================


@router.delete(
    "/{challenge_public_id}",
    response_model=ApiResponse,
    summary="Bekleyen challenge'ı iptal eder",
)
async def cancel_challenge(
    challenge_public_id: str,
    request_body: CancelChallengeRequest | None = None,
    context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_backend_user,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Yalnızca token sahibine ait bekleyen challenge
    iptal edilebilir.

    Challenge'ın bağlı cihazı bulunmayabilir. Bu durum
    demo challenge akışında normaldir.
    """

    try:
        current_result = (
            challenge_service
            .get_challenge_status(
                db,
                challenge_public_id=(
                    challenge_public_id
                ),
            )
        )

    except ChallengeNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception


    ensure_challenge_owner(
        challenge=current_result.challenge,
        external_user_id=(
            context.user.external_user_id
        ),
    )


    try:
        challenge = await (
            challenge_service.cancel_challenge(
                db,
                challenge_public_id=(
                    challenge_public_id
                ),
                reason=(
                    request_body.reason
                    if request_body is not None
                    else None
                ),
            )
        )

    except ChallengeAlreadyCompletedError as exception:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception


    return ApiResponse(
        success=True,
        message=(
            "Doğrulama isteği iptal edildi."
        ),
        data=map_challenge_response(
            challenge=challenge,
            delivered_to_device=False,
        ).model_dump(
            mode="json",
        ),
        errors={},
    )