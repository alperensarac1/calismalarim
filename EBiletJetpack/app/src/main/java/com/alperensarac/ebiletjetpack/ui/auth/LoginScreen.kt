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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.alperensarac.ebiletjetpack.ui.components.AppLinkButton
import com.alperensarac.ebiletjetpack.ui.components.AppPrimaryButton
import com.alperensarac.ebiletjetpack.ui.components.AppTextDark
import com.alperensarac.ebiletjetpack.ui.components.AppTextField
import com.alperensarac.ebiletjetpack.ui.components.AppTextGray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    LoginScreen

    Compose giriş ekranıdır.

    Görevleri:
    - E-posta ve şifre state'lerini tutmak
    - Form doğrulama yapmak
    - Retrofit ile auth/login.php API'sine istek atmak
    - Başarılı olursa SessionManager ile kullanıcıyı kaydetmek
    - Home ekranına yönlendirmek

    XML'deki EditText mantığı Compose'ta state ile çalışır.

    Örnek:
    var email by remember { mutableStateOf("") }

    Kullanıcı yazdıkça:
    email = yeniDeger
*/
@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    /*
        SessionManager context ister.
        remember ile ekranda tekrar tekrar oluşturulmasını azaltıyoruz.
    */
    val sessionManager = remember {
        SessionManager(context)
    }

    /*
        Form state'leri.
    */
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    /*
        Hata state'leri.
        True olursa ilgili input kırmızı görünür.
    */
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    /*
        Loading state.
        API isteği devam ederken buton pasif olur.
    */
    var isLoading by remember { mutableStateOf(false) }

    /*
        Kullanıcı daha önce giriş yaptıysa direkt Home'a gönder.
    */
    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Etkinlik Bileti",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AppTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Etkinlikleri keşfet, biletini QR kodla kullan.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextGray
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                        text = "Giriş Yap",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTextDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

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
                        text = "Giriş Yap",
                        isLoading = isLoading,
                        onClick = {
                            /*
                                Butona basıldığında form kontrolü + API çağrısı yapıyoruz.
                            */
                            emailError = false
                            passwordError = false

                            val cleanEmail = email.trim()
                            val cleanPassword = password.trim()

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

                            /*
                                Retrofit login isteği.
                            */
                            ApiClient.apiService
                                .login(cleanEmail, cleanPassword)
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
                                            "Giriş başarılı. Hoş geldin ${user.fullName}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        onLoginSuccess()
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
                            text = "Hesabın yok mu? Kayıt ol",
                            enabled = !isLoading,
                            onClick = onGoRegister
                        )
                    }
                }
            }
        }
    }
}