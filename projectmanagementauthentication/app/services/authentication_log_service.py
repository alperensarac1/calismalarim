from __future__ import annotations

import math

from dataclasses import dataclass
from datetime import datetime

from sqlalchemy import (
    case,
    func,
    or_,
    select,
)
from sqlalchemy.orm import Session
from sqlalchemy.sql import Select

from app.models import (
    AuthenticationLog,
    AuthenticationMethod,
    AuthenticationResult,
    DevicePlatform,
)


# =========================================================
# SERVİS HATALARI
# =========================================================


class AuthenticationLogServiceError(Exception):
    """
    Güvenlik logu işlemlerinde kullanılan
    temel hata sınıfıdır.
    """

    pass


class AuthenticationLogNotFoundError(
    AuthenticationLogServiceError,
):
    """
    İstenen güvenlik logu bulunamadığında yükseltilir.
    """

    pass


class InvalidAuthenticationLogDateRangeError(
    AuthenticationLogServiceError,
):
    """
    Başlangıç tarihi bitiş tarihinden sonra olduğunda
    yükseltilir.
    """

    pass


# =========================================================
# SONUÇ MODELLERİ
# =========================================================


@dataclass(frozen=True, slots=True)
class AuthenticationLogListResult:
    """
    Sayfalı güvenlik logu sorgusunun sonucudur.
    """

    items: list[AuthenticationLog]

    page: int

    page_size: int

    total_count: int

    total_pages: int

    has_previous_page: bool

    has_next_page: bool


@dataclass(frozen=True, slots=True)
class AuthenticationLogSummaryResult:
    """
    Admin ekranında gösterilebilecek genel güvenlik
    istatistiklerini temsil eder.
    """

    total_count: int

    success_count: int

    failed_count: int

    rejected_count: int

    low_risk_count: int

    medium_risk_count: int

    high_risk_count: int

    critical_risk_count: int

    location_mismatch_count: int


# =========================================================
# YARDIMCI FONKSİYONLAR
# =========================================================


def normalize_optional_text(
    value: str | None,
) -> str | None:
    """
    Opsiyonel metin değerini temizler.

    Boş veya yalnızca boşluk içeren değerlerde
    None döndürür.
    """

    if value is None:
        return None

    normalized_value = value.strip()

    return normalized_value or None


def validate_date_range(
    *,
    start_date: datetime | None,
    end_date: datetime | None,
) -> None:
    """
    Tarih aralığının geçerli olup olmadığını
    kontrol eder.
    """

    if (
        start_date is not None
        and end_date is not None
        and start_date > end_date
    ):
        raise InvalidAuthenticationLogDateRangeError(
            "Başlangıç tarihi bitiş tarihinden "
            "sonra olamaz.",
        )


def apply_log_filters(
    statement: Select,
    *,
    external_user_id: str | None,
    email: str | None,
    result: AuthenticationResult | None,
    method: AuthenticationMethod | None,
    platform: DevicePlatform | None,
    risk_level: str | None,
    request_ip: str | None,
    has_location: bool | None,
    location_mismatch: bool | None,
    city: str | None,
    country_code: str | None,
    search: str | None,
    start_date: datetime | None,
    end_date: datetime | None,
) -> Select:
    """
    Güvenlik logu sorgusuna yalnızca gönderilen
    filtreleri uygular.
    """

    normalized_external_user_id = (
        normalize_optional_text(
            external_user_id,
        )
    )

    normalized_email = normalize_optional_text(
        email,
    )

    normalized_risk_level = normalize_optional_text(
        risk_level,
    )

    normalized_request_ip = normalize_optional_text(
        request_ip,
    )

    normalized_city = normalize_optional_text(
        city,
    )

    normalized_country_code = normalize_optional_text(
        country_code,
    )

    normalized_search = normalize_optional_text(
        search,
    )

    if normalized_external_user_id is not None:
        statement = statement.where(
            AuthenticationLog.external_user_id_snapshot
            == normalized_external_user_id,
        )

    if normalized_email is not None:
        statement = statement.where(
            func.lower(
                AuthenticationLog.email_snapshot,
            )
            == normalized_email.lower(),
        )

    if result is not None:
        statement = statement.where(
            AuthenticationLog.result == result,
        )

    if method is not None:
        statement = statement.where(
            AuthenticationLog.method == method,
        )

    if platform is not None:
        statement = statement.where(
            AuthenticationLog.platform_snapshot
            == platform.value,
        )

    if normalized_risk_level is not None:
        statement = statement.where(
            func.lower(
                AuthenticationLog.risk_level,
            )
            == normalized_risk_level.lower(),
        )

    if normalized_request_ip is not None:
        statement = statement.where(
            AuthenticationLog.request_ip
            == normalized_request_ip,
        )

    if has_location is True:
        statement = statement.where(
            AuthenticationLog.latitude.is_not(
                None,
            ),
            AuthenticationLog.longitude.is_not(
                None,
            ),
        )

    elif has_location is False:
        statement = statement.where(
            or_(
                AuthenticationLog.latitude.is_(
                    None,
                ),
                AuthenticationLog.longitude.is_(
                    None,
                ),
            ),
        )

    if location_mismatch is not None:
        statement = statement.where(
            AuthenticationLog.location_mismatch
            == location_mismatch,
        )

    if normalized_city is not None:
        city_pattern = (
            f"%{normalized_city.lower()}%"
        )

        statement = statement.where(
            or_(
                func.lower(
                    AuthenticationLog.gps_city,
                ).like(
                    city_pattern,
                ),
                func.lower(
                    AuthenticationLog.gps_district,
                ).like(
                    city_pattern,
                ),
                func.lower(
                    AuthenticationLog.ip_city,
                ).like(
                    city_pattern,
                ),
            ),
        )

    if normalized_country_code is not None:
        country_code_value = (
            normalized_country_code.upper()
        )

        statement = statement.where(
            or_(
                AuthenticationLog.gps_country_code
                == country_code_value,
                AuthenticationLog.ip_country_code
                == country_code_value,
            ),
        )

    if normalized_search is not None:
        search_pattern = (
            f"%{normalized_search.lower()}%"
        )

        statement = statement.where(
            or_(
                func.lower(
                    AuthenticationLog
                    .display_name_snapshot,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.email_snapshot,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog
                    .device_name_snapshot,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog
                    .device_model_snapshot,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.request_ip,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.device_ip,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.gps_city,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.gps_district,
                ).like(
                    search_pattern,
                ),
                func.lower(
                    AuthenticationLog.gps_country,
                ).like(
                    search_pattern,
                ),
            ),
        )

    if start_date is not None:
        statement = statement.where(
            AuthenticationLog.created_at
            >= start_date,
        )

    if end_date is not None:
        statement = statement.where(
            AuthenticationLog.created_at
            <= end_date,
        )

    return statement


# =========================================================
# SERVİS
# =========================================================


class AuthenticationLogService:
    """
    Admin güvenlik kayıtlarının listelenmesi,
    filtrelenmesi ve özetlenmesinden sorumludur.
    """

    def get_logs(
        self,
        db: Session,
        *,
        page: int,
        page_size: int,
        external_user_id: str | None = None,
        email: str | None = None,
        result: AuthenticationResult | None = None,
        method: AuthenticationMethod | None = None,
        platform: DevicePlatform | None = None,
        risk_level: str | None = None,
        request_ip: str | None = None,
        has_location: bool | None = None,
        location_mismatch: bool | None = None,
        city: str | None = None,
        country_code: str | None = None,
        search: str | None = None,
        start_date: datetime | None = None,
        end_date: datetime | None = None,
    ) -> AuthenticationLogListResult:
        """
        Güvenlik loglarını filtreli ve sayfalı olarak
        getirir.

        En yeni kayıtlar önce döndürülür.
        """

        validate_date_range(
            start_date=start_date,
            end_date=end_date,
        )

        base_statement = select(
            AuthenticationLog,
        )

        filtered_statement = apply_log_filters(
            base_statement,
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

        count_statement = select(
            func.count(
                AuthenticationLog.id,
            ),
        )

        count_statement = apply_log_filters(
            count_statement,
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

        total_count = (
            db.scalar(
                count_statement,
            )
            or 0
        )

        total_pages = (
            math.ceil(
                total_count / page_size,
            )
            if total_count > 0
            else 0
        )

        offset = (
            page - 1
        ) * page_size

        paged_statement = (
            filtered_statement
            .order_by(
                AuthenticationLog.created_at.desc(),
                AuthenticationLog.id.desc(),
            )
            .offset(
                offset,
            )
            .limit(
                page_size,
            )
        )

        items = list(
            db.scalars(
                paged_statement,
            ).all()
        )

        has_previous_page = (
            total_pages > 0
            and page > 1
        )

        has_next_page = (
            total_pages > 0
            and page < total_pages
        )

        return AuthenticationLogListResult(
            items=items,
            page=page,
            page_size=page_size,
            total_count=total_count,
            total_pages=total_pages,
            has_previous_page=has_previous_page,
            has_next_page=has_next_page,
        )

    def get_log_by_public_id(
        self,
        db: Session,
        *,
        log_public_id: str,
    ) -> AuthenticationLog:
        """
        Public ID değerine göre tek bir güvenlik
        logu getirir.
        """

        normalized_public_id = (
            log_public_id.strip()
        )

        if not normalized_public_id:
            raise AuthenticationLogNotFoundError(
                "Güvenlik logu kimliği boş olamaz.",
            )

        statement = select(
            AuthenticationLog,
        ).where(
            AuthenticationLog.public_id
            == normalized_public_id,
        )

        authentication_log = db.scalar(
            statement,
        )

        if authentication_log is None:
            raise AuthenticationLogNotFoundError(
                "Güvenlik logu bulunamadı.",
            )

        return authentication_log

    def get_summary(
        self,
        db: Session,
        *,
        start_date: datetime | None = None,
        end_date: datetime | None = None,
    ) -> AuthenticationLogSummaryResult:
        """
        Admin ekranı için temel güvenlik
        istatistiklerini döndürür.
        """

        validate_date_range(
            start_date=start_date,
            end_date=end_date,
        )

        statement = select(
            func.count(
                AuthenticationLog.id,
            ).label(
                "total_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.result
                        == AuthenticationResult.SUCCESS,
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "success_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.result
                        == AuthenticationResult.FAILED,
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "failed_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.result
                        == AuthenticationResult.REJECTED,
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "rejected_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.risk_level
                        == "low",
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "low_risk_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.risk_level
                        == "medium",
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "medium_risk_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.risk_level
                        == "high",
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "high_risk_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog.risk_level
                        == "critical",
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "critical_risk_count",
            ),

            func.sum(
                case(
                    (
                        AuthenticationLog
                        .location_mismatch.is_(
                            True,
                        ),
                        1,
                    ),
                    else_=0,
                )
            ).label(
                "location_mismatch_count",
            ),
        )

        if start_date is not None:
            statement = statement.where(
                AuthenticationLog.created_at
                >= start_date,
            )

        if end_date is not None:
            statement = statement.where(
                AuthenticationLog.created_at
                <= end_date,
            )

        row = db.execute(
            statement,
        ).one()

        return AuthenticationLogSummaryResult(
            total_count=(
                row.total_count or 0
            ),
            success_count=(
                row.success_count or 0
            ),
            failed_count=(
                row.failed_count or 0
            ),
            rejected_count=(
                row.rejected_count or 0
            ),
            low_risk_count=(
                row.low_risk_count or 0
            ),
            medium_risk_count=(
                row.medium_risk_count or 0
            ),
            high_risk_count=(
                row.high_risk_count or 0
            ),
            critical_risk_count=(
                row.critical_risk_count or 0
            ),
            location_mismatch_count=(
                row.location_mismatch_count or 0
            ),
        )


authentication_log_service = (
    AuthenticationLogService()
)