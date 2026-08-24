from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec


def main() -> None:
    """
    Swagger cihaz kayıt testi için geçici
    ECDSA P-256 anahtar çifti üretir.

    Gerçek Android/iOS uygulamasında private key
    cihazın güvenli anahtar deposunda üretilecek ve
    hiçbir zaman dışarı çıkarılmayacaktır.
    """

    private_key = ec.generate_private_key(
        ec.SECP256R1(),
    )

    private_key_pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=(
            serialization.PrivateFormat.PKCS8
        ),
        encryption_algorithm=(
            serialization.NoEncryption()
        ),
    )

    public_key_pem = private_key.public_key().public_bytes(
        encoding=serialization.Encoding.PEM,
        format=(
            serialization.PublicFormat
            .SubjectPublicKeyInfo
        ),
    )

    with open(
        "test_device_private_key.pem",
        "wb",
    ) as private_key_file:
        private_key_file.write(
            private_key_pem,
        )

    with open(
        "test_device_public_key.pem",
        "wb",
    ) as public_key_file:
        public_key_file.write(
            public_key_pem,
        )

    print(
        "Test cihaz anahtarları oluşturuldu.",
    )

    print(
        "\nPUBLIC KEY:\n",
    )

    print(
        public_key_pem.decode(
            "utf-8",
        ),
    )

    print(
        "Private key yalnızca test için "
        "dosyaya yazıldı.",
    )


if __name__ == "__main__":
    main()