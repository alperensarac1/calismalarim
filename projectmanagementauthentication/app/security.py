from __future__ import annotations

import base64
import hashlib
import hmac
import secrets

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Any

import jwt

from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric.ec import (
    ECDSA,
    EllipticCurvePublicKey,
)

from app.config import get_settings


settings = get_settings()


class SecurityError(Exception):
    """
    Güvenlik işlemlerinde kullanılan temel hata sınıfıdır.
    """

    pass


class InvalidDeviceTokenError(SecurityError):
    """
    Cihaz access tokenı geçersiz olduğunda yükseltilir.
    """

    pass


class ExpiredDeviceTokenError(InvalidDeviceTokenError):
    """
    Cihaz access tokenının süresi dolduğunda yükseltilir.
    """

    pass


class InvalidPublicKeyError(SecurityError):
    """
    Cihazın gönderdiği public key geçersiz olduğunda
    yükseltilir.
    """

    pass


class InvalidDeviceSignatureError(SecurityError):
    """
    Challenge imzası doğrulanamadığında yükseltilir.
    """

    pass


@dataclass(frozen=True, slots=True)
class DeviceTokenPayload:
    """
    Doğrulanmış cihaz access tokenı içerisindeki
    güvenilir bilgileri temsil eder.
    """

    device_public_id: str

    external_user_id: str

    installation_id: str

    platform: str

    issued_at: datetime

    expires_at: datetime

    token_id: str


def utc_now() -> datetime:
    """
    UTC zaman dilimindeki güncel zamanı döndürür.
    """

    return datetime.now(timezone.utc)


def generate_one_time_code(
    length: int = 6,
) -> str:
    """
    Kriptografik olarak güvenli sayısal doğrulama
    kodu üretir.

    Varsayılan olarak altı haneli kod üretir.
    """

    if length < 6 or length > 12:
        raise ValueError(
            "Doğrulama kodu uzunluğu 6 ile 12 arasında olmalıdır.",
        )

    return "".join(
        secrets.choice("0123456789")
        for _ in range(length)
    )


def generate_random_token(
    byte_length: int = 32,
) -> str:
    """
    Nonce, token ID veya bağlantı anahtarı olarak
    kullanılabilecek rastgele URL-safe değer üretir.
    """

    if byte_length < 16:
        raise ValueError(
            "Rastgele token uzunluğu en az 16 byte olmalıdır.",
        )

    return secrets.token_urlsafe(
        byte_length,
    )


def _derive_code_hash(
    code: str,
    salt: bytes,
) -> bytes:
    """
    PBKDF2-HMAC-SHA256 ile tek kullanımlık kodun
    hash değerini üretir.
    """

    return hashlib.pbkdf2_hmac(
        "sha256",
        code.encode("utf-8"),
        salt,
        210_000,
    )


def hash_one_time_code(
    code: str,
) -> str:
    """
    Tek kullanımlık kodu salt kullanarak hashler.

    Veritabanında şu biçimde saklanır:

    pbkdf2_sha256$iterations$salt$hash
    """

    normalized_code = code.strip()

    if not normalized_code.isdigit():
        raise ValueError(
            "Doğrulama kodu yalnızca rakamlardan oluşmalıdır.",
        )

    salt = secrets.token_bytes(16)

    code_hash = _derive_code_hash(
        normalized_code,
        salt,
    )

    encoded_salt = base64.urlsafe_b64encode(
        salt,
    ).decode("ascii")

    encoded_hash = base64.urlsafe_b64encode(
        code_hash,
    ).decode("ascii")

    return (
        "pbkdf2_sha256"
        "$210000"
        f"${encoded_salt}"
        f"${encoded_hash}"
    )


def verify_one_time_code(
    code: str,
    stored_hash: str,
) -> bool:
    """
    Kullanıcının gönderdiği doğrulama kodunu
    veritabanındaki hash ile karşılaştırır.
    """

    try:
        algorithm, iterations, encoded_salt, encoded_hash = (
            stored_hash.split(
                "$",
                maxsplit=3,
            )
        )

        if algorithm != "pbkdf2_sha256":
            return False

        if int(iterations) != 210_000:
            return False

        salt = base64.urlsafe_b64decode(
            encoded_salt.encode("ascii"),
        )

        expected_hash = base64.urlsafe_b64decode(
            encoded_hash.encode("ascii"),
        )

    except (
        ValueError,
        TypeError,
        base64.binascii.Error,
    ):
        return False

    calculated_hash = _derive_code_hash(
        code.strip(),
        salt,
    )

    return hmac.compare_digest(
        calculated_hash,
        expected_hash,
    )


def create_device_access_token(
    *,
    device_public_id: str,
    external_user_id: str,
    installation_id: str,
    platform: str,
) -> tuple[str, datetime]:
    """
    Kayıtlı mobil cihaz için uzun ömürlü JWT üretir.

    Bu token yalnızca Python Authenticator Service
    tarafından kullanılacaktır.
    """

    issued_at = utc_now()

    expires_at = issued_at + timedelta(
        days=settings.device_token_expire_days,
    )

    token_id = generate_random_token(
        24,
    )

    payload: dict[str, Any] = {
        "sub": device_public_id,
        "user_id": external_user_id,
        "installation_id": installation_id,
        "platform": platform,
        "purpose": "authenticator_device",
        "jti": token_id,
        "iat": issued_at,
        "nbf": issued_at,
        "exp": expires_at,
        "iss": settings.app_name,
        "aud": "authenticator-device",
    }

    token = jwt.encode(
        payload,
        settings.secret_key,
        algorithm=settings.jwt_algorithm,
    )

    return token, expires_at


def decode_device_access_token(
    token: str,
) -> DeviceTokenPayload:
    """
    Mobil cihaz JWT'sini doğrular ve güvenilir
    payload modeline dönüştürür.
    """

    normalized_token = token.strip()

    if not normalized_token:
        raise InvalidDeviceTokenError(
            "Cihaz access tokenı boş olamaz.",
        )

    try:
        payload = jwt.decode(
            normalized_token,
            settings.secret_key,
            algorithms=[
                settings.jwt_algorithm,
            ],
            audience="authenticator-device",
            issuer=settings.app_name,
        )

    except jwt.ExpiredSignatureError as exception:
        raise ExpiredDeviceTokenError(
            "Cihaz access tokenının süresi dolmuş.",
        ) from exception

    except jwt.InvalidTokenError as exception:
        raise InvalidDeviceTokenError(
            "Cihaz access tokenı geçersiz.",
        ) from exception

    if payload.get("purpose") != "authenticator_device":
        raise InvalidDeviceTokenError(
            "Token cihaz doğrulaması için üretilmemiş.",
        )

    device_public_id = str(
        payload.get("sub", ""),
    ).strip()

    external_user_id = str(
        payload.get("user_id", ""),
    ).strip()

    installation_id = str(
        payload.get("installation_id", ""),
    ).strip()

    platform = str(
        payload.get("platform", ""),
    ).strip()

    token_id = str(
        payload.get("jti", ""),
    ).strip()

    if not all(
        (
            device_public_id,
            external_user_id,
            installation_id,
            platform,
            token_id,
        ),
    ):
        raise InvalidDeviceTokenError(
            "Cihaz tokenında gerekli alanlar eksik.",
        )

    issued_at = datetime.fromtimestamp(
        int(payload["iat"]),
        tz=timezone.utc,
    )

    expires_at = datetime.fromtimestamp(
        int(payload["exp"]),
        tz=timezone.utc,
    )

    return DeviceTokenPayload(
        device_public_id=device_public_id,
        external_user_id=external_user_id,
        installation_id=installation_id,
        platform=platform,
        issued_at=issued_at,
        expires_at=expires_at,
        token_id=token_id,
    )


def load_ec_public_key(
    public_key_pem: str,
) -> EllipticCurvePublicKey:
    """
    PEM biçimindeki ECDSA public key'i yükler.

    Android ve iOS istemcilerinin P-256 anahtar
    üretmesini bekleyeceğiz.
    """

    try:
        loaded_key = serialization.load_pem_public_key(
            public_key_pem.strip().encode(
                "utf-8",
            ),
        )

    except (
        ValueError,
        TypeError,
    ) as exception:
        raise InvalidPublicKeyError(
            "Public key geçerli PEM biçiminde değil.",
        ) from exception

    if not isinstance(
        loaded_key,
        EllipticCurvePublicKey,
    ):
        raise InvalidPublicKeyError(
            "Public key ECDSA elliptic curve anahtarı değil.",
        )

    curve_name = loaded_key.curve.name.lower()

    if curve_name not in {
        "secp256r1",
        "prime256v1",
    }:
        raise InvalidPublicKeyError(
            "Yalnızca P-256 ECDSA public key destekleniyor.",
        )

    return loaded_key


def calculate_public_key_fingerprint(
    public_key_pem: str,
) -> str:
    """
    Public key için SHA-256 parmak izi üretir.

    Aynı anahtar tekrar kaydedilmeye çalışıldığında
    karşılaştırma amacıyla kullanılabilir.
    """

    public_key = load_ec_public_key(
        public_key_pem,
    )

    public_key_der = public_key.public_bytes(
        encoding=serialization.Encoding.DER,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    )

    digest = hashlib.sha256(
        public_key_der,
    ).hexdigest()

    grouped_digest = ":".join(
        digest[index:index + 2].upper()
        for index in range(
            0,
            len(digest),
            2,
        )
    )

    return f"SHA256:{grouped_digest}"


def build_challenge_signing_payload(
    *,
    challenge_public_id: str,
    nonce: str,
    external_user_id: str,
    installation_id: str,
    decision: str,
    expires_at: datetime,
) -> bytes:
    """
    Android ve iOS'un aynı şekilde imzalayabileceği
    sabit challenge payloadını üretir.

    Alan sırası değiştirilmemelidir.
    """

    normalized_decision = decision.strip().lower()

    if normalized_decision not in {
        "approve",
        "reject",
    }:
        raise ValueError(
            "Karar approve veya reject olmalıdır.",
        )

    expires_at_utc = expires_at.astimezone(
        timezone.utc,
    )

    payload = "\n".join(
        [
            f"challenge_id={challenge_public_id}",
            f"nonce={nonce}",
            f"user_id={external_user_id}",
            f"installation_id={installation_id}",
            f"decision={normalized_decision}",
            (
                "expires_at="
                f"{expires_at_utc.isoformat()}"
            ),
        ],
    )

    return payload.encode(
        "utf-8",
    )


def verify_device_signature(
    *,
    public_key_pem: str,
    payload: bytes,
    signature_base64: str,
) -> bool:
    """
    Cihazın challenge payloadı üzerinde oluşturduğu
    ECDSA-SHA256 imzasını doğrular.
    """

    public_key = load_ec_public_key(
        public_key_pem,
    )

    try:
        signature = base64.b64decode(
            signature_base64.strip(),
            validate=True,
        )

    except (
        ValueError,
        base64.binascii.Error,
    ) as exception:
        raise InvalidDeviceSignatureError(
            "Cihaz imzası geçerli Base64 biçiminde değil.",
        ) from exception

    try:
        public_key.verify(
            signature,
            payload,
            ECDSA(
                hashes.SHA256(),
            ),
        )

    except InvalidSignature as exception:
        raise InvalidDeviceSignatureError(
            "Cihaz imzası doğrulanamadı.",
        ) from exception

    return True