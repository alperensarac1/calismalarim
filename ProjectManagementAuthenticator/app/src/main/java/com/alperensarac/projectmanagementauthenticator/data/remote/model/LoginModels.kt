package com.alperensarac.projectmanagementauthenticator.data.remote.model

import com.google.gson.annotations.SerializedName


/*
 * =========================================================
 * LOGIN REQUEST MODELİ
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasından mevcut .NET
 * ProjectManagement API'sine gönderilecek giriş
 * isteğini temsil eder.
 *
 * Beklenen JSON:
 *
 * {
 *   "email": "admin@projectmanagement.local",
 *   "password": "123456"
 * }
 */
data class LoginRequest(
    /**
     * Kullanıcının .NET backend hesabındaki
     * e-posta adresidir.
     */
    @SerializedName("email")
    val email: String,

    /**
     * Kullanıcının .NET backend hesabındaki şifresidir.
     */
    @SerializedName("password")
    val password: String,
) {
    /**
     * E-posta ve şifre değerlerini temizlenmiş biçimde
     * yeni bir LoginRequest nesnesine dönüştürür.
     */
    fun normalized(): LoginRequest {
        return copy(
            email = email.trim(),
            password = password,
        )
    }


    /**
     * Giriş isteğinin temel olarak geçerli olup
     * olmadığını kontrol eder.
     *
     * Bu kontrol sunucu doğrulamasının yerine geçmez.
     * Yalnızca boş alanlarla gereksiz API isteği
     * gönderilmesini önler.
     */
    fun validate(): LoginValidationResult {
        val normalizedEmail =
            email.trim()

        if (normalizedEmail.isBlank()) {
            return LoginValidationResult.Invalid(
                message = "E-posta adresi boş olamaz.",
            )
        }

        if (!normalizedEmail.contains("@")) {
            return LoginValidationResult.Invalid(
                message = "Geçerli bir e-posta adresi giriniz.",
            )
        }

        if (password.isBlank()) {
            return LoginValidationResult.Invalid(
                message = "Şifre boş olamaz.",
            )
        }

        return LoginValidationResult.Valid
    }
}


/*
 * =========================================================
 * LOGIN VALIDATION MODELİ
 * =========================================================
 */


/**
 * Login formunun yerel doğrulama sonucunu temsil eder.
 */
sealed interface LoginValidationResult {
    /**
     * Form alanları temel kontrollerden geçti.
     */
    data object Valid : LoginValidationResult

    /**
     * Form alanlarından biri geçersiz.
     */
    data class Invalid(
        val message: String,
    ) : LoginValidationResult
}


/*
 * =========================================================
 * LOGIN RESPONSE MODELİ
 * =========================================================
 */


/**
 * .NET login endpointinin ApiResponse.data alanında
 * dönen bilgileri temsil eder.
 *
 * Backend yapısına göre tokenlar doğrudan data içinde
 * veya nested bir token nesnesinde dönebilir.
 *
 * Bu model iki yapıyı da destekleyecek şekilde
 * hazırlanmıştır.
 */
data class LoginResponseData(
    /**
     * .NET API access tokenı.
     *
     * Gson alternate sayesinde backend alanı
     * accessToken, token veya jwtToken olarak gelirse
     * okunabilir.
     */
    @SerializedName(
        value = "accessToken",
        alternate = [
            "AccessToken",
            "token",
            "Token",
            "jwtToken",
            "JwtToken",
        ],
    )
    val accessToken: String? = null,

    /**
     * .NET API refresh tokenı.
     */
    @SerializedName(
        value = "refreshToken",
        alternate = [
            "RefreshToken",
        ],
    )
    val refreshToken: String? = null,

    /**
     * Access tokenın geçerlilik bitiş zamanı.
     *
     * ISO 8601 string veya backend tarafından
     * kullanılan tarih metni olabilir.
     */
    @SerializedName(
        value = "accessTokenExpiresAt",
        alternate = [
            "AccessTokenExpiresAt",
            "expiresAt",
            "ExpiresAt",
            "expiration",
            "Expiration",
        ],
    )
    val accessTokenExpiresAt: String? = null,

    /**
     * Refresh tokenın geçerlilik bitiş zamanı.
     */
    @SerializedName(
        value = "refreshTokenExpiresAt",
        alternate = [
            "RefreshTokenExpiresAt",
        ],
    )
    val refreshTokenExpiresAt: String? = null,

    /**
     * Kullanıcı bilgileri nested user nesnesinde
     * dönüyorsa bu alana eşlenir.
     */
    @SerializedName(
        value = "user",
        alternate = [
            "User",
        ],
    )
    val user: LoginUserData? = null,

    /**
     * Bazı backend yapılarında kullanıcı alanları
     * doğrudan login data nesnesi içinde olabilir.
     */
    @SerializedName(
        value = "id",
        alternate = [
            "Id",
            "userId",
            "UserId",
        ],
    )
    val directUserId: String? = null,

    @SerializedName(
        value = "email",
        alternate = [
            "Email",
            "userEmail",
            "UserEmail",
        ],
    )
    val directEmail: String? = null,

    @SerializedName(
        value = "fullName",
        alternate = [
            "FullName",
            "displayName",
            "DisplayName",
            "name",
            "Name",
        ],
    )
    val directDisplayName: String? = null,

    @SerializedName(
        value = "firstName",
        alternate = [
            "FirstName",
        ],
    )
    val directFirstName: String? = null,

    @SerializedName(
        value = "lastName",
        alternate = [
            "LastName",
        ],
    )
    val directLastName: String? = null,

    @SerializedName(
        value = "role",
        alternate = [
            "Role",
            "userRole",
            "UserRole",
        ],
    )
    val directRole: String? = null,

    @SerializedName(
        value = "isActive",
        alternate = [
            "IsActive",
            "active",
            "Active",
        ],
    )
    val directIsActive: Boolean? = null,

    /**
     * Backend tokenları ayrı bir nested nesne içinde
     * döndürüyorsa bu alan kullanılır.
     *
     * Örnek:
     *
     * {
     *   "data": {
     *     "tokens": {
     *       "accessToken": "...",
     *       "refreshToken": "..."
     *     }
     *   }
     * }
     */
    @SerializedName(
        value = "tokens",
        alternate = [
            "Tokens",
            "tokenData",
            "TokenData",
        ],
    )
    val tokens: LoginTokenData? = null,
) {
    /**
     * Kullanılabilir access tokenı döndürür.
     *
     * Önce doğrudan accessToken alanına, ardından
     * nested tokens alanına bakılır.
     */
    fun resolveAccessToken(): String? {
        return accessToken
            .normalizeNullable()
            ?: tokens
                ?.accessToken
                .normalizeNullable()
    }


    /**
     * Kullanılabilir refresh tokenı döndürür.
     */
    fun resolveRefreshToken(): String? {
        return refreshToken
            .normalizeNullable()
            ?: tokens
                ?.refreshToken
                .normalizeNullable()
    }


    /**
     * Kullanıcı ID değerini çözümler.
     */
    fun resolveUserId(): String? {
        return user
            ?.id
            .normalizeNullable()
            ?: directUserId
                .normalizeNullable()
    }


    /**
     * Kullanıcı e-posta adresini çözümler.
     */
    fun resolveEmail(): String? {
        return user
            ?.email
            .normalizeNullable()
            ?: directEmail
                .normalizeNullable()
    }


    /**
     * Kullanıcının görünen adını çözümler.
     *
     * Öncelik sırası:
     *
     * 1. Nested user.fullName
     * 2. Nested user.firstName + lastName
     * 3. Doğrudan fullName
     * 4. Doğrudan firstName + lastName
     */
    fun resolveDisplayName(): String? {
        val nestedDisplayName =
            user?.resolveDisplayName()

        if (!nestedDisplayName.isNullOrBlank()) {
            return nestedDisplayName
        }

        val directFullName =
            directDisplayName.normalizeNullable()

        if (directFullName != null) {
            return directFullName
        }

        return combineNameParts(
            firstName = directFirstName,
            lastName = directLastName,
        )
    }


    /**
     * Kullanıcı rolünü çözümler.
     */
    fun resolveRole(): String? {
        return user
            ?.role
            .normalizeNullable()
            ?: directRole
                .normalizeNullable()
    }


    /**
     * Kullanıcının aktiflik durumunu çözümler.
     *
     * Backend alanı göndermiyorsa başarılı login
     * cevabı alındığı için true kabul edilir.
     */
    fun resolveIsActive(): Boolean {
        return user?.isActive
            ?: directIsActive
            ?: true
    }


    /**
     * Login cevabının gerekli access tokenı içerip
     * içermediğini kontrol eder.
     */
    fun hasValidAccessToken(): Boolean {
        return !resolveAccessToken().isNullOrBlank()
    }
}


/*
 * =========================================================
 * TOKEN MODELİ
 * =========================================================
 */


/**
 * Login cevabındaki nested token modelidir.
 */
data class LoginTokenData(
    @SerializedName(
        value = "accessToken",
        alternate = [
            "AccessToken",
            "token",
            "Token",
        ],
    )
    val accessToken: String? = null,

    @SerializedName(
        value = "refreshToken",
        alternate = [
            "RefreshToken",
        ],
    )
    val refreshToken: String? = null,

    @SerializedName(
        value = "accessTokenExpiresAt",
        alternate = [
            "AccessTokenExpiresAt",
            "expiresAt",
            "ExpiresAt",
        ],
    )
    val accessTokenExpiresAt: String? = null,

    @SerializedName(
        value = "refreshTokenExpiresAt",
        alternate = [
            "RefreshTokenExpiresAt",
        ],
    )
    val refreshTokenExpiresAt: String? = null,
)


/*
 * =========================================================
 * KULLANICI MODELİ
 * =========================================================
 */


/**
 * Login cevabında dönebilecek kullanıcı bilgilerini
 * temsil eder.
 */
data class LoginUserData(
    /**
     * .NET backend kullanıcı ID değeri.
     *
     * Backend GUID veya sayısal ID kullanabileceği için
     * String olarak tanımlanmıştır.
     */
    @SerializedName(
        value = "id",
        alternate = [
            "Id",
            "userId",
            "UserId",
        ],
    )
    val id: String? = null,

    /**
     * Kullanıcının e-posta adresi.
     */
    @SerializedName(
        value = "email",
        alternate = [
            "Email",
            "userEmail",
            "UserEmail",
        ],
    )
    val email: String? = null,

    /**
     * Kullanıcının doğrudan tam adı.
     */
    @SerializedName(
        value = "fullName",
        alternate = [
            "FullName",
            "displayName",
            "DisplayName",
            "name",
            "Name",
        ],
    )
    val fullName: String? = null,

    /**
     * Kullanıcının adı.
     */
    @SerializedName(
        value = "firstName",
        alternate = [
            "FirstName",
        ],
    )
    val firstName: String? = null,

    /**
     * Kullanıcının soyadı.
     */
    @SerializedName(
        value = "lastName",
        alternate = [
            "LastName",
        ],
    )
    val lastName: String? = null,

    /**
     * Kullanıcının sistem rolü.
     */
    @SerializedName(
        value = "role",
        alternate = [
            "Role",
            "userRole",
            "UserRole",
        ],
    )
    val role: String? = null,

    /**
     * Bazı backend cevaplarında roller liste olarak
     * dönebilir.
     */
    @SerializedName(
        value = "roles",
        alternate = [
            "Roles",
        ],
    )
    val roles: List<String>? = null,

    /**
     * Kullanıcının aktiflik durumu.
     */
    @SerializedName(
        value = "isActive",
        alternate = [
            "IsActive",
            "active",
            "Active",
        ],
    )
    val isActive: Boolean? = null,
) {
    /**
     * Kullanıcının görünen adını oluşturur.
     */
    fun resolveDisplayName(): String? {
        val normalizedFullName =
            fullName.normalizeNullable()

        if (normalizedFullName != null) {
            return normalizedFullName
        }

        return combineNameParts(
            firstName = firstName,
            lastName = lastName,
        )
    }


    /**
     * Kullanıcının rol bilgisini çözümler.
     *
     * Doğrudan role alanı yoksa roles listesi virgülle
     * birleştirilir.
     */
    fun resolveRole(): String? {
        val directRole =
            role.normalizeNullable()

        if (directRole != null) {
            return directRole
        }

        return roles
            ?.mapNotNull {
                it.normalizeNullable()
            }
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.joinToString(
                separator = ",",
            )
    }
}


/*
 * =========================================================
 * LOGIN SONUÇ MODELİ
 * =========================================================
 */


/**
 * Repository katmanının ViewModel'e döndürebileceği
 * giriş sonucunu temsil eder.
 *
 * API response modelinin doğrudan ekrana taşınması
 * yerine uygulama için sade bir sonuç modeli kullanılır.
 */
sealed interface LoginResult {
    /**
     * Giriş başarıyla tamamlandı.
     */
    data class Success(
        val accessToken: String,
        val refreshToken: String?,
        val userId: String?,
        val email: String?,
        val displayName: String?,
        val role: String?,
        val isActive: Boolean,
    ) : LoginResult

    /**
     * Giriş başarısız oldu.
     */
    data class Failure(
        val message: String,
        val httpStatusCode: Int? = null,
    ) : LoginResult
}


/*
 * =========================================================
 * YARDIMCI FONKSİYONLAR
 * =========================================================
 */


/**
 * Nullable String değerini temizler.
 *
 * Null veya boş string için null döndürür.
 */
private fun String?.normalizeNullable(): String? {
    return this
        ?.trim()
        ?.takeIf {
            it.isNotBlank()
        }
}


/**
 * Ad ve soyadı birleştirerek görünen ad üretir.
 */
private fun combineNameParts(
    firstName: String?,
    lastName: String?,
): String? {
    val parts =
        listOfNotNull(
            firstName.normalizeNullable(),
            lastName.normalizeNullable(),
        )

    if (parts.isEmpty()) {
        return null
    }

    return parts.joinToString(
        separator = " ",
    )
}