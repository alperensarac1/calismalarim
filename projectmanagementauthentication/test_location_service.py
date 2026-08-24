import asyncio

from app.services.location_service import (
    LocationServiceError,
    location_service,
)


async def main() -> None:
    # Örnek koordinatlar.
    # Kendi telefon koordinatınla da test edebilirsin.
    latitude = 41.159
    longitude = 27.802

    print(
        "Konum çözümleniyor..."
    )

    try:
        result = await (
            location_service.resolve_coordinates(
                latitude=latitude,
                longitude=longitude,
            )
        )

    except LocationServiceError as exception:
        print(
            f"Konum çözümlenemedi: {exception}"
        )
        return

    if result is None:
        print(
            "Konum sonucu bulunamadı."
        )
        return

    print()
    print(
        f"Şehir: {result.city}"
    )
    print(
        f"İlçe: {result.district}"
    )
    print(
        f"Bölge: {result.region}"
    )
    print(
        f"Ülke: {result.country}"
    )
    print(
        f"Ülke kodu: {result.country_code}"
    )
    print(
        f"Tam adres: {result.display_name}"
    )


if __name__ == "__main__":
    asyncio.run(
        main(),
    )