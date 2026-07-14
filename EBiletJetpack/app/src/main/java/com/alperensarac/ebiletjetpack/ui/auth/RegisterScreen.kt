package com.alperensarac.ebiletjetpack.ui.auth


import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.alperensarac.ebiletjetpack.data.api.ApiClient
import com.alperensarac.ebiletjetpack.data.model.ApiResponse
import com.alperensarac.ebiletjetpack.data.model.User
import com.alperensarac.ebiletjetpack.data.session.SessionManager
import com.alperensarac.ebiletjetpack.ui.components.AppBackground
import com.alperensarac.ebiletjetpack.ui.components.AppGreen
import com.alperensarac.ebiletjetpack.ui.components.AppLinkButton
import com.alperensarac.ebiletjetpack.ui.components.AppPrimaryButton
import com.alperensarac.ebiletjetpack.ui.components.AppTextDark
import com.alperensarac.ebiletjetpack.ui.components.AppTextField
import com.alperensarac.ebiletjetpack.ui.components.AppTextGray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    RegisterScreen

    Compose kayıt ekranıdır.

    Görevleri:
    - Ad soyad, e-posta, telefon, şifre almak
    - Form validasyonu yapmak
    - auth/register.php API'sine istek atmak
    - Başarılı olursa kullanıcıyı SessionManager ile kaydetmek
    - Home ekranına geçmek
*/
@Composable
fun RegisterScreen(
    onGoLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    /*
        Form state'leri.
    */
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    /*
        Hata state'leri.
    */
    var fullNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yeni Hesap Oluştur",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Etkinlik biletlerini kolayca satın almak için kayıt ol.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Kayıt Ol",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTextDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AppTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = false
                        },
                        label = "Ad Soyad",
                        isError = fullNameError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        label = "E-posta",
                        isError = emailError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = false
                        },
                        label = "Telefon",
                        isError = phoneError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        label = "Şifre",
                        isError = passwordError,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AppPrimaryButton(
                        text = "Kayıt Ol",
                        backgroundColor = AppGreen,
                        isLoading = isLoading,
                        onClick = {
                            fullNameError = false
                            emailError = false
                            phoneError = false
                            passwordError = false

                            val cleanFullName = fullName.trim()
                            val cleanEmail = email.trim()
                            val cleanPhone = phone.trim()
                            val cleanPassword = password.trim()

                            if (cleanFullName.isEmpty()) {
                                fullNameError = true
                                Toast.makeText(context, "Ad soyad zorunludur", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            if (cleanFullName.length < 3) {
                                fullNameError = true
                                Toast.makeText(context, "Ad soyad en az 3 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            if (cleanEmail.isEmpty()) {
                                emailError = true
                                Toast.makeText(context, "E-posta zorunludur", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                emailError = true
                                Toast.makeText(context, "Geçerli bir e-posta giriniz", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            /*
                                Telefon zorunlu değil.
                                Ama girilmişse kısa olmamalı.
                            */
                            if (cleanPhone.isNotEmpty() && cleanPhone.length < 10) {
                                phoneError = true
                                Toast.makeText(context, "Telefon numarası eksik görünüyor", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            if (cleanPassword.isEmpty()) {
                                passwordError = true
                                Toast.makeText(context, "Şifre zorunludur", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            if (cleanPassword.length < 6) {
                                passwordError = true
                                Toast.makeText(context, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                                return@AppPrimaryButton
                            }

                            isLoading = true

                            ApiClient.apiService
                                .register(
                                    fullName = cleanFullName,
                                    email = cleanEmail,
                                    phone = cleanPhone,
                                    password = cleanPassword
                                )
                                .enqueue(object : Callback<ApiResponse<User>> {

                                    override fun onResponse(
                                        call: Call<ApiResponse<User>>,
                                        response: Response<ApiResponse<User>>
                                    ) {
                                        isLoading = false

                                        if (!response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Sunucu hatası: ${response.code()}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }

                                        val apiResponse = response.body()

                                        if (apiResponse == null) {
                                            Toast.makeText(
                                                context,
                                                "Boş sunucu cevabı",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }

                                        if (!apiResponse.success) {
                                            Toast.makeText(
                                                context,
                                                apiResponse.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }

                                        val user = apiResponse.data

                                        if (user == null) {
                                            Toast.makeText(
                                                context,
                                                "Kullanıcı bilgisi alınamadı",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }

                                        sessionManager.saveUser(user)

                                        Toast.makeText(
                                            context,
                                            "Kayıt başarılı. Hoş geldin ${user.fullName}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        onRegisterSuccess()
                                    }

                                    override fun onFailure(
                                        call: Call<ApiResponse<User>>,
                                        t: Throwable
                                    ) {
                                        isLoading = false

                                        Toast.makeText(
                                            context,
                                            "Bağlantı hatası: ${t.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                })
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLinkButton(
                            text = "Zaten hesabın var mı? Giriş yap",
                            enabled = !isLoading,
                            onClick = onGoLogin
                        )
                    }
                }
            }
        }
    }
}