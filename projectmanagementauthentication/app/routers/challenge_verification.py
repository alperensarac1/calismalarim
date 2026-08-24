from __future__ import annotations

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Request,
    status,
)
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas import (
    ApiResponse,
    ChallengeDecisionRequest,
    ChallengeVerificationResponse,
    VerifyChallengeCodeRequest,
)
from app.services.challenge_service import (
    ChallengeAlreadyCompletedError,
    ChallengeExpiredError,
    ChallengeNotFoundError,
)
from app.services.challenge_verification_service import (
    ChallengeDeviceMismatchError,
    ChallengeLockedError,
    ChallengeVerificationError,
    InvalidChallengeCodeError,
    InvalidChallengeDecisionError,
    MissingDevicePublicKeyError,
    challenge_verification_service,
)
from app.routers.challenges import (
    AuthenticatedBackendUserContext,
    get_authenticated_backend_user,
)
from app.routers.devices import (
    AuthenticatedDeviceContext,
    get_authenticated_device_context,
    get_request_ip,
)
from app.websocket_manager import websocket_manager


router = APIRouter(
    prefix="/api/challenges",
    tags=["Challenge Verification"],
)


def get_user_agent(
    request: Request,
) -> str | None:
    """
    HTTP isteğinin User-Agent bilgisini döndürür.
    """

    value = request.headers.get(
        "user-agent",
    )

    if value is None:
        return None

    normalized_value = value.strip()

    return normalized_value or None


def map_verification_response(
    result,
) -> ChallengeVerificationResponse:
    """
    Servis sonucunu güvenli API response modeline
    dönüştürür.
    """

    challenge = result.challenge
    authentication_log = result.authentication_log

    return ChallengeVerificationResponse(
        challenge_public_id=challenge.public_id,
        status=challenge.status,
        result=result.attempt.result,
        is_successful=result.is_successful,
        attempt_count=challenge.attempt_count,
        max_attempts=challenge.max_attempts,
        device_signature_verified=(
            challenge.device_signature_verified
        ),
        completed_at=challenge.completed_at,
        failure_reason=result.attempt.failure_reason,
        risk_score=authentication_log.risk_score,
        risk_level=authentication_log.risk_level,
    )


async def notify_challenge_result(
    *,
    challenge,
    result,
) -> None:
    """
    Challenge sonucu oluştuğunda hedef cihaza
    WebSocket üzerinden bilgi gönderir.

    WebSocket bağlantısı kapalı olsa bile doğrulama
    sonucu veritabanında korunur.
    """

    target_device = challenge.target_device

    if target_device is None:
        return

    await websocket_manager.send_json_to_device(
        target_device.public_id,
        {
            "type": "challenge_result",
            "challenge_public_id": (
                challenge.public_id
            ),
            "status": challenge.status.value,
            "result": result.attempt.result.value,
            "is_successful": result.is_successful,
            "attempt_count": (
                challenge.attempt_count
            ),
            "max_attempts": (
                challenge.max_attempts
            ),
            "completed_at": (
                challenge.completed_at.isoformat()
                if challenge.completed_at is not None
                else None
            ),
        },
    )


@router.post(
    "/{challenge_public_id}/verify-code",
    response_model=ApiResponse,
    summary=(
        "Tek kullanımlık doğrulama kodunu kontrol eder"
    ),
)
async def verify_challenge_code(
    challenge_public_id: str,
    request_body: VerifyChallengeCodeRequest,
    request: Request,
    context: AuthenticatedBackendUserContext = Depends(
        get_authenticated_backend_user,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Authenticator cihazında gösterilen tek kullanımlık
    kodu doğrular.

    Bu endpoint mevcut .NET backend access tokenını
    Authorization başlığında bekler:

    Authorization: Bearer <backend-access-token>
    """

    source_ip = get_request_ip(
        request,
    )

    user_agent = get_user_agent(
        request,
    )

    try:
        result = (
            challenge_verification_service
            .verify_code(
                db,
                challenge_public_id=(
                    challenge_public_id
                ),
                external_user_id=(
                    context.user.external_user_id
                ),
                code=request_body.code,
                source_ip=source_ip,
                user_agent=user_agent,
            )
        )

    except ChallengeNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeExpiredError as exception:
        raise HTTPException(
            status_code=status.HTTP_410_GONE,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeLockedError as exception:
        raise HTTPException(
            status_code=status.HTTP_423_LOCKED,
            detail=str(
                exception,
            ),
        ) from exception

    except InvalidChallengeCodeError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeAlreadyCompletedError as exception:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeVerificationError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    await notify_challenge_result(
        challenge=result.challenge,
        result=result,
    )

    response_data = map_verification_response(
        result,
    )

    return ApiResponse(
        success=True,
        message=(
            "Doğrulama kodu başarıyla onaylandı."
        ),
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )


@router.post(
    "/{challenge_public_id}/decision",
    response_model=ApiResponse,
    summary=(
        "Mobil cihaz onay veya ret kararı gönderir"
    ),
)
async def process_challenge_decision(
    challenge_public_id: str,
    request_body: ChallengeDecisionRequest,
    request: Request,
    context: AuthenticatedDeviceContext = Depends(
        get_authenticated_device_context,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Authenticator mobil uygulamasının verdiği onay veya
    ret kararını işler.

    Bu endpoint mevcut .NET tokenını değil, cihaz kaydı
    sonucunda Python servisinin ürettiği device tokenını
    bekler:

    Authorization: Bearer <device-access-token>

    Karar ayrıca cihazın private key'iyle imzalanmalıdır.
    """

    if (
        request_body.installation_id
        != context.device.installation_id
    ):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Request installation ID bilgisi "
                "doğrulanmış cihazla eşleşmiyor."
            ),
        )

    source_ip = get_request_ip(
        request,
    )

    user_agent = get_user_agent(
        request,
    )

    try:
        # process_device_decision artık reverse geocoding
        # yaptığı için async çalışmaktadır.
        result = await (
            challenge_verification_service
            .process_device_decision(
                db,
                challenge_public_id=(
                    challenge_public_id
                ),
                device=context.device,
                decision=request_body.decision,
                signature=request_body.signature,
                source_ip=source_ip,
                user_agent=user_agent,
                latitude=request_body.latitude,
                longitude=request_body.longitude,
                location_accuracy_meters=(
                    request_body
                    .location_accuracy_meters
                ),
                location_permission_status=(
                    request_body
                    .location_permission_status
                ),
                location_captured_at=(
                    request_body
                    .location_captured_at
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

    except ChallengeExpiredError as exception:
        raise HTTPException(
            status_code=status.HTTP_410_GONE,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeAlreadyCompletedError as exception:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeDeviceMismatchError as exception:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=str(
                exception,
            ),
        ) from exception

    except MissingDevicePublicKeyError as exception:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(
                exception,
            ),
        ) from exception

    except InvalidChallengeDecisionError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except ChallengeVerificationError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    await notify_challenge_result(
        challenge=result.challenge,
        result=result,
    )

    response_data = map_verification_response(
        result,
    )

    if result.is_successful:
        message = (
            "Doğrulama isteği mobil cihaz "
            "tarafından onaylandı."
        )
    else:
        message = (
            "Doğrulama isteği mobil cihaz "
            "tarafından reddedildi."
        )

    return ApiResponse(
        success=True,
        message=message,
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )