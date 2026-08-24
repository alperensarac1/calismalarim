from __future__ import annotations

import asyncio
import math

from dataclasses import dataclass
from functools import lru_cache
from typing import Any

from geopy.exc import (
    GeocoderServiceError,
    GeocoderTimedOut,
    GeocoderUnavailable,
)
from geopy.geocoders import Nominatim

from app.config import get_settings


settings = get_settings()


# =========================================================
# SERVİS HATALARI
# =========================================================


class LocationServiceError(Exception):
    """
    Konum çözümleme işlemlerinde kullanılan
    temel hata sınıfıdır.
    """

    pass


class InvalidCoordinatesError(
    LocationServiceError,
):
    """
    Enlem veya boylam değeri geçersiz olduğunda
    yükseltilir.
    """

    pass


class ReverseGeocodingUnavailableError(
    LocationServiceError,
):
    """
    Reverse geocoding servisine erişilemediğinde
    yükseltilir.
    """

    pass


# =========================================================
# SONUÇ MODELİ
# =========================================================


@dataclass(frozen=True, slots=True)
class ResolvedLocation:
    """
    GPS koordinatlarından çözümlenen platformdan
    bağımsız konum bilgisidir.

    raw_address alanı, Nominatim servisinin döndürdüğü
    ham adres bileşenlerini içerir.
    """

    latitude: float

    longitude: float

    city: str | None

    district: str | None

    region: str | None

    country: str | None

    country_code: str | None

    postal_code: str | None

    display_name: str | None

    provider: str

    raw_address: dict[str, Any]


# =========================================================
# GEOCODER NESNESİ
# =========================================================


@lru_cache(
    maxsize=1,
)
def get_geocoder() -> Nominatim:
    """
    Nominatim geocoder nesnesini uygulama boyunca
    yalnızca bir defa oluşturur.

    Her reverse-geocoding çağrısında yeniden nesne
    oluşturulmasını engeller.
    """

    return Nominatim(
        user_agent=(
            settings.reverse_geocoding_user_agent
        ),
        timeout=(
            settings.reverse_geocoding_timeout_seconds
        ),
    )


# =========================================================
# METİN YARDIMCILARI
# =========================================================


def normalize_optional_text(
    value: object,
) -> str | None:
    """
    Gelen değeri temizlenmiş string biçimine
    dönüştürür.

    None veya boş string değerlerinde None döndürür.
    """

    if value is None:
        return None

    normalized_value = str(
        value,
    ).strip()

    return normalized_value or None


def first_address_value(
    address: dict[str, Any],
    *keys: str,
) -> str | None:
    """
    Nominatim address sözlüğünde verilen alanlardan
    ilk dolu değeri getirir.

    Farklı ülkelerde veya bölgelerde aynı idari alan
    farklı anahtarlarla dönebildiği için birden fazla
    alan sırasıyla kontrol edilir.
    """

    for key in keys:
        value = normalize_optional_text(
            address.get(
                key,
            ),
        )

        if value:
            return value

    return None


def normalize_country_code(
    value: object,
) -> str | None:
    """
    Ülke kodunu büyük harfli biçime dönüştürür.

    Örneğin:

    tr -> TR
    """

    normalized_value = normalize_optional_text(
        value,
    )

    if normalized_value is None:
        return None

    return normalized_value.upper()


# =========================================================
# KOORDİNAT YARDIMCILARI
# =========================================================


def validate_coordinates(
    *,
    latitude: float,
    longitude: float,
) -> None:
    """
    Enlem ve boylam değerlerinin geçerli olup
    olmadığını kontrol eder.

    Geçerli aralıklar:

    Enlem:
    -90 ile 90

    Boylam:
    -180 ile 180
    """

    if not math.isfinite(
        latitude,
    ):
        raise InvalidCoordinatesError(
            "Enlem değeri sonlu bir sayı olmalıdır.",
        )

    if not math.isfinite(
        longitude,
    ):
        raise InvalidCoordinatesError(
            "Boylam değeri sonlu bir sayı olmalıdır.",
        )

    if not -90 <= latitude <= 90:
        raise InvalidCoordinatesError(
            "Enlem değeri -90 ile 90 arasında olmalıdır.",
        )

    if not -180 <= longitude <= 180:
        raise InvalidCoordinatesError(
            "Boylam değeri -180 ile 180 arasında olmalıdır.",
        )


def round_coordinate(
    value: float,
) -> float:
    """
    Koordinatı dört ondalık basamağa yuvarlar.

    Dört ondalık basamak yaklaşık 10-15 metre
    düzeyinde hassasiyet sağlar.

    Bu yuvarlama:

    - Cache kullanımını artırır.
    - Çok yakın koordinatlar için tekrar tekrar dış
      servis çağrısı yapılmasını azaltır.
    """

    return round(
        value,
        4,
    )


# =========================================================
# SENKRON REVERSE GEOCODING
# =========================================================


@lru_cache(
    maxsize=settings.reverse_geocoding_cache_size,
)
def reverse_geocode_sync(
    latitude: float,
    longitude: float,
) -> ResolvedLocation | None:
    """
    Koordinatı Nominatim servisi üzerinden senkron
    olarak çözümler.

    Bu fonksiyon cache'lidir.

    Async endpoint içerisinden doğrudan çağrılmaz.
    LocationService.resolve_coordinates metodu bu
    işlemi ayrı bir thread içerisinde çalıştırır.
    """

    if not settings.reverse_geocoding_enabled:
        return None

    validate_coordinates(
        latitude=latitude,
        longitude=longitude,
    )

    geocoder = get_geocoder()

    try:
        location = geocoder.reverse(
            query=(
                latitude,
                longitude,
            ),
            exactly_one=True,
            language="tr",
            addressdetails=True,
            zoom=18,
        )

    except (
        GeocoderTimedOut,
        GeocoderUnavailable,
        GeocoderServiceError,
        OSError,
    ) as exception:
        raise ReverseGeocodingUnavailableError(
            "Reverse geocoding servisine erişilemedi.",
        ) from exception

    except Exception as exception:
        raise LocationServiceError(
            "Konum bilgisi çözümlenirken beklenmeyen "
            "bir hata oluştu.",
        ) from exception

    if location is None:
        return None

    raw_data = (
        location.raw
        if isinstance(
            location.raw,
            dict,
        )
        else {}
    )

    raw_address_value = raw_data.get(
        "address",
        {},
    )

    address: dict[str, Any] = (
        raw_address_value
        if isinstance(
            raw_address_value,
            dict,
        )
        else {}
    )

    # Şehir bilgisi ülkeye ve OpenStreetMap verisinin
    # yapısına göre farklı alanlarda bulunabilir.
    city = first_address_value(
        address,
        "city",
        "province",
        "town",
        "municipality",
        "county",
    )

    # İlçe veya alt idari bölge bilgisi farklı
    # anahtarlarla dönebilir.
    district = first_address_value(
        address,
        "district",
        "city_district",
        "borough",
        "suburb",
        "county",
        "town",
        "village",
    )

    # Bölge, eyalet veya il bilgisi.
    region = first_address_value(
        address,
        "state",
        "region",
        "province",
    )

    country = first_address_value(
        address,
        "country",
    )

    country_code = normalize_country_code(
        address.get(
            "country_code",
        ),
    )

    postal_code = first_address_value(
        address,
        "postcode",
    )

    display_name = normalize_optional_text(
        raw_data.get(
            "display_name",
        ),
    )

    return ResolvedLocation(
        latitude=latitude,
        longitude=longitude,
        city=city,
        district=district,
        region=region,
        country=country,
        country_code=country_code,
        postal_code=postal_code,
        display_name=display_name,
        provider="nominatim",
        raw_address=address,
    )


# =========================================================
# ASYNC KONUM SERVİSİ
# =========================================================


class LocationService:
    """
    GPS koordinatlarını şehir, ilçe, bölge ve ülke
    bilgisine dönüştüren servis sınıfıdır.
    """

    async def resolve_coordinates(
        self,
        *,
        latitude: float | None,
        longitude: float | None,
    ) -> ResolvedLocation | None:
        """
        Koordinatlar mevcutsa reverse-geocoding
        işlemini gerçekleştirir.

        Geopy senkron ağ çağrısı yaptığı için işlem
        asyncio.to_thread ile ayrı bir thread üzerinde
        çalıştırılır.

        Böylece FastAPI event loop'u ağ çağrısı
        sırasında bloke edilmez.
        """

        if (
            latitude is None
            or longitude is None
        ):
            return None

        if not settings.reverse_geocoding_enabled:
            return None

        validate_coordinates(
            latitude=latitude,
            longitude=longitude,
        )

        normalized_latitude = round_coordinate(
            latitude,
        )

        normalized_longitude = round_coordinate(
            longitude,
        )

        return await asyncio.to_thread(
            reverse_geocode_sync,
            normalized_latitude,
            normalized_longitude,
        )

    def clear_cache(
        self,
    ) -> None:
        """
        Reverse-geocoding sonucunun bellek içi
        cache kayıtlarını temizler.

        Testlerde veya ayar değişikliklerinden sonra
        kullanılabilir.
        """

        reverse_geocode_sync.cache_clear()

    def get_cache_info(
        self,
    ) -> object:
        """
        Reverse-geocoding cache istatistiklerini
        döndürür.

        Dönen değer hit, miss, maxsize ve currsize
        bilgilerini içerir.
        """

        return reverse_geocode_sync.cache_info()


location_service = LocationService()