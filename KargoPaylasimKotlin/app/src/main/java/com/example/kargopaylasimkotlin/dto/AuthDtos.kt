package com.example.kargopaylasimkotlin.dto

/**
 * Ortak alan interface'leri (profesyonel/temiz yapı)
 */
interface UserIdentity {
    val first_name: String
    val last_name: String
}

interface HasPhone {
    val phone: String
}

interface HasCredentials : HasPhone {
    val password: String
}

data class RegisterReq(
    override val first_name: String,
    override val last_name: String,
    override val phone: String,
    val tc_no: String,
    override val password: String,
    // address
    val address_title: String,
    val city: String,
    val district: String,
    val neighborhood: String? = null,
    val address_line: String,
    val postal_code: String? = null
) : UserIdentity, HasCredentials

data class RegisterResp(
    val user_id: Int,
    val address_id: Int
)

data class LoginReq(
    override val phone: String,
    override val password: String
) : HasCredentials

data class LoginResp(
    val token: String
)

data class UserMeResp(
    val user: UserDto
)

data class UserDto(
    val id: Int,
    override val first_name: String,
    override val last_name: String,
    val phone_e164: String
) : UserIdentity

/**
 * Not: LoginRequest, LoginReq ile aynı işi yaptığı için kaldırmak en temiz çözüm.
 * Eğer projede başka yerler hâlâ LoginRequest kullanıyorsa geçiş sürecinde kalsın diye
 * aşağıdaki gibi LoginReq'e alias olarak bırakıyoruz.
 */
typealias LoginRequest = LoginReq
