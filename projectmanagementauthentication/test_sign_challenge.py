import base64
from datetime import datetime
from pathlib import Path

from cryptography.hazmat.primitives import (
    hashes,
    serialization,
)
from cryptography.hazmat.primitives.asymmetric.ec import (
    ECDSA,
    EllipticCurvePrivateKey,
)

from app.security import (
    build_challenge_signing_payload,
)


PRIVATE_KEY_PATH = Path(
    "test_device_private_key.pem",
)


def load_private_key() -> EllipticCurvePrivateKey:
    """
    Test amacıyla oluşturulan ECDSA private key'i
    PEM dosyasından yükler.

    Gerçek mobil uygulamada private key dosyadan
    okunmayacak; Android Keystore veya iOS Secure
    Enclave içinde tutulacaktır.
    """

    if not PRIVATE_KEY_PATH.exists():
        raise FileNotFoundError(
            f"Private key dosyası bulunamadı: "
            f"{PRIVATE_KEY_PATH.resolve()}"
        )

    private_key_bytes = (
        PRIVATE_KEY_PATH.read_bytes()
    )

    loaded_key = (
        serialization.load_pem_private_key(
            private_key_bytes,
            password=None,
        )
    )

    if not isinstance(
        loaded_key,
        EllipticCurvePrivateKey,
    ):
        raise TypeError(
            "Yüklenen private key ECDSA anahtarı değil."
        )

    return loaded_key


def read_required_value(
    prompt: str,
) -> str:
    """
    Kullanıcıdan boş olmayan bir değer alır.
    """

    while True:
        value = input(
            prompt,
        ).strip()

        if value:
            return value

        print(
            "Bu alan boş bırakılamaz."
        )


def read_decision() -> str:
    """
    approve veya reject kararını kullanıcıdan alır.
    """

    while True:
        value = input(
            "Karar [approve/reject]: ",
        ).strip().lower()

        if value in {
            "approve",
            "reject",
        }:
            return value

        print(
            "Karar yalnızca approve veya reject olabilir."
        )


def parse_expires_at(
    value: str,
) -> datetime:
    """
    Swagger veya WebSocket mesajındaki ISO tarih
    değerini datetime nesnesine dönüştürür.

    Z ile biten UTC formatını da destekler.
    """

    normalized_value = value.strip()

    if normalized_value.endswith(
        "Z",
    ):
        normalized_value = (
            normalized_value[:-1]
            + "+00:00"
        )

    parsed_value = datetime.fromisoformat(
        normalized_value,
    )

    if parsed_value.tzinfo is None:
        raise ValueError(
            "expires_at timezone bilgisi içermelidir."
        )

    return parsed_value


def main() -> None:
    """
    Challenge payloadını test private key ile imzalar
    ve Swagger karar endpointinde kullanılacak Base64
    imzayı üretir.
    """

    print()
    print(
        "Challenge imzalama testi"
    )
    print(
        "=" * 60
    )

    challenge_public_id = read_required_value(
        "Challenge public ID: ",
    )

    nonce = read_required_value(
        "Nonce: ",
    )

    external_user_id = read_required_value(
        "External user ID: ",
    )

    installation_id = read_required_value(
        "Installation ID: ",
    )

    decision = read_decision()

    expires_at_text = read_required_value(
        "Expires at: ",
    )

    expires_at = parse_expires_at(
        expires_at_text,
    )

    payload = build_challenge_signing_payload(
        challenge_public_id=challenge_public_id,
        nonce=nonce,
        external_user_id=external_user_id,
        installation_id=installation_id,
        decision=decision,
        expires_at=expires_at,
    )

    private_key = load_private_key()

    signature = private_key.sign(
        payload,
        ECDSA(
            hashes.SHA256(),
        ),
    )

    signature_base64 = (
        base64.b64encode(
            signature,
        ).decode(
            "ascii",
        )
    )

    print()
    print(
        "=" * 60
    )
    print(
        "İMZALANAN PAYLOAD"
    )
    print(
        "=" * 60
    )
    print(
        payload.decode(
            "utf-8",
        )
    )

    print()
    print(
        "=" * 60
    )
    print(
        "BASE64 İMZA"
    )
    print(
        "=" * 60
    )
    print(
        signature_base64
    )

    print()
    print(
        "Swagger decision body örneği:"
    )

    print(
        "{"
    )
    print(
        f'  "decision": "{decision}",'
    )
    print(
        f'  "installation_id": '
        f'"{installation_id}",'
    )
    print(
        f'  "signature": '
        f'"{signature_base64}",'
    )
    print(
        '  "latitude": 41.159,'
    )
    print(
        '  "longitude": 27.802,'
    )
    print(
        '  "location_accuracy_meters": 100,'
    )
    print(
        '  "location_permission_status": '
        '"granted_approximate",'
    )
    print(
        '  "location_captured_at": '
        '"2026-07-31T10:30:00Z"'
    )
    print(
        "}"
    )


if __name__ == "__main__":
    try:
        main()

    except Exception as exception:
        print()
        print(
            "İmzalama işlemi başarısız:"
        )
        print(
            f"{type(exception).__name__}: "
            f"{exception}"
        )