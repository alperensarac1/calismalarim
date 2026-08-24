package com.alperensarac.projectmanagementauthenticator.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.alperensarac.projectmanagementauthenticator.data.repository.AuthRepository
import com.alperensarac.projectmanagementauthenticator.data.repository.AuthenticatorLoginResult
import com.alperensarac.projectmanagementauthenticator.data.repository.AuthenticatorLoginStage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/*
 * =========================================================
 * LOGIN EKRAN DURUMU
 * =========================================================
 */


/**
 * Login ekranının bütün durumunu tek bir immutable
 * model içerisinde tutar.
 *
 * Activity veya Fragment doğrudan Repository sonucunu
 * yönetmez. Yalnızca bu state nesnesini gözlemler.
 *
 * Böylece:
 *
 * - Loading durumu
 * - Form değerleri
 * - Hata mesajları
 * - Cihaz kayıt aşaması
 * - Başarılı giriş
 *
 * tek merkezden yönetilebilir.
 */
data class LoginUiState(
    /**
     * Kullanıcının girdiği e-posta adresi.
     */
    val email: String = "",

    /**
     * Kullanıcının girdiği şifre.
     *
     * Gerçek uygulamada state içerisinde tutulması
     * normaldir; ancak kalıcı depolamaya yazılmamalıdır.
     */
    val password: String = "",

    /**
     * Login veya cihaz kayıt işlemi devam ederken true
     * olur.
     *
     * Bu durumda giriş butonu pasif hâle getirilebilir
     * ve progress indicator gösterilebilir.
     */
    val isLoading: Boolean = false,

    /**
     * .NET backend login işleminin başarılı olup
     * olmadığını belirtir.
     */
    val isBackendLoginCompleted: Boolean = false,

    /**
     * Python Authenticator cihaz kaydının tamamlanıp
     * tamamlanmadığını belirtir.
     */
    val isDeviceRegistrationCompleted: Boolean = false,

    /**
     * Login ve cihaz kayıt işlemlerinin tamamının
     * başarılı olup olmadığını belirtir.
     *
     * true olduğunda uygulama WebSocket veya ana ekran
     * bölümüne geçebilir.
     */
    val isLoginCompleted: Boolean = false,

    /**
     * Kullanıcıya gösterilecek hata mesajı.
     */
    val errorMessage: String? = null,

    /**
     * Kullanıcıya gösterilebilecek başarı veya bilgi
     * mesajı.
     */
    val infoMessage: String? = null,

    /**
     * Hatanın login aşamasında mı yoksa cihaz kayıt
     * aşamasında mı oluştuğunu belirtir.
     */
    val failedStage: AuthenticatorLoginStage? = null,

    /**
     * .NET login başarılı olup yalnızca cihaz kaydı
     * başarısızsa true olur.
     *
     * Bu değer true olduğunda kullanıcıya şifreyi tekrar
     * girmeden "Cihaz kaydını tekrar dene" butonu
     * gösterilebilir.
     */
    val canRetryDeviceRegistration: Boolean = false,

    /**
     * Başarılı login sonrasında kullanıcı adı.
     */
    val displayName: String? = null,

    /**
     * Başarılı login sonrasında kullanıcı e-postası.
     */
    val authenticatedEmail: String? = null,

    /**
     * Başarılı cihaz kaydı sonrasında cihaz public ID
     * değeri.
     */
    val devicePublicId: String? = null,

    /**
     * Cihaz kaydının backend tarafından döndürülen
     * açıklama mesajı.
     */
    val deviceRegistrationMessage: String? = null,
) {
    /**
     * Login butonunun aktif olup olmayacağını belirtir.
     */
    val isLoginButtonEnabled: Boolean
        get() {
            return (
                    !isLoading &&
                            email.isNotBlank() &&
                            password.isNotBlank()
                    )
        }

    /**
     * Cihaz kayıt tekrar deneme butonunun aktif olup
     * olmayacağını belirtir.
     */
    val isRetryDeviceButtonEnabled: Boolean
        get() {
            return (
                    !isLoading &&
                            canRetryDeviceRegistration
                    )
        }
}


/*
 * =========================================================
 * LOGIN VIEWMODEL
 * =========================================================
 */


/**
 * Login ekranındaki kullanıcı etkileşimlerini ve
 * Repository çağrılarını yöneten ViewModel sınıfıdır.
 *
 * Bu ViewModel:
 *
 * - E-posta alanını günceller
 * - Şifre alanını günceller
 * - Formu doğrular
 * - .NET login işlemini başlatır
 * - Python cihaz kaydını başlatır
 * - Loading ve hata durumlarını yönetir
 * - Cihaz kayıt tekrar denemesini yönetir
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /*
     * ViewModel içerisinde değiştirilebilir state.
     *
     * Dışarıya MutableStateFlow açmıyoruz. Böylece ekran
     * state değerini yalnızca okuyabilir; doğrudan
     * değiştiremez.
     */
    private val _uiState =
        MutableStateFlow(
            LoginUiState(),
        )

    /**
     * Activity veya Fragment tarafından gözlemlenecek
     * salt okunur state akışı.
     */
    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()


    /*
     * =====================================================
     * FORM DEĞİŞİKLİKLERİ
     * =====================================================
     */


    /**
     * Kullanıcı e-posta alanını değiştirdiğinde çağrılır.
     */
    fun onEmailChanged(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                email = value,

                /*
                 * Kullanıcı formu yeniden düzenlemeye
                 * başladığında eski hata mesajını
                 * temizliyoruz.
                 */
                errorMessage = null,

                failedStage = null,
            )
    }


    /**
     * Kullanıcı şifre alanını değiştirdiğinde çağrılır.
     */
    fun onPasswordChanged(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                password = value,
                errorMessage = null,
                failedStage = null,
            )
    }


    /*
     * =====================================================
     * LOGIN AKIŞI
     * =====================================================
     */


    /**
     * Kullanıcı giriş butonuna bastığında çağrılır.
     *
     * İşlem sırası:
     *
     * 1. Aynı anda ikinci isteğin başlaması engellenir.
     * 2. Form alanları yerel olarak kontrol edilir.
     * 3. Repository.loginAndRegisterDevice çağrılır.
     * 4. Repository önce .NET login yapar.
     * 5. Ardından cihazı Python servisine kaydeder.
     * 6. Sonuca göre ekran state'i güncellenir.
     */
    fun login() {
        val currentState =
            _uiState.value

        /*
         * Devam eden işlem varken tekrar login isteği
         * göndermiyoruz.
         */
        if (currentState.isLoading) {
            return
        }


        val normalizedEmail =
            currentState.email.trim()

        val password =
            currentState.password


        /*
         * Repository içerisinde de doğrulama vardır.
         * Buradaki kontrol kullanıcıya daha hızlı geri
         * bildirim vermek içindir.
         */
        val validationMessage =
            validateLoginForm(
                email = normalizedEmail,
                password = password,
            )

        if (validationMessage != null) {
            _uiState.value =
                currentState.copy(
                    email = normalizedEmail,
                    errorMessage = validationMessage,
                    infoMessage = null,
                    failedStage =
                    AuthenticatorLoginStage.LOGIN,
                )

            return
        }


        viewModelScope.launch {
            /*
             * Ağ işlemi başlamadan önce loading state.
             */
            _uiState.value =
                _uiState.value.copy(
                    email = normalizedEmail,
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = (
                            "Project Management hesabı "
                                    + "doğrulanıyor..."
                            ),
                    failedStage = null,
                    canRetryDeviceRegistration = false,
                    isBackendLoginCompleted = false,
                    isDeviceRegistrationCompleted = false,
                    isLoginCompleted = false,
                )


            val result =
                authRepository.loginAndRegisterDevice(
                    email = normalizedEmail,
                    password = password,
                )


            when (result) {
                is AuthenticatorLoginResult.Success -> {
                    handleLoginSuccess(
                        result = result,
                    )
                }

                is AuthenticatorLoginResult.Failure -> {
                    handleLoginFailure(
                        result = result,
                    )
                }
            }
        }
    }


    /*
     * =====================================================
     * CİHAZ KAYDINI TEKRAR DENEME
     * =====================================================
     */


    /**
     * .NET login başarılı fakat Python cihaz kaydı
     * başarısız olduğunda yalnızca cihaz kayıt işlemini
     * tekrar dener.
     *
     * Kullanıcının e-posta ve şifreyi yeniden girmesine
     * gerek kalmaz.
     */
    fun retryDeviceRegistration() {
        val currentState =
            _uiState.value

        if (
            currentState.isLoading ||
            !currentState.canRetryDeviceRegistration
        ) {
            return
        }


        viewModelScope.launch {
            _uiState.value =
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = (
                            "Authenticator cihazı yeniden "
                                    + "kaydediliyor..."
                            ),
                    failedStage = null,
                )


            val result =
                authRepository.registerCurrentDevice()


            when (result) {
                is com.alperensarac
                .projectmanagementauthenticator
                .data.remote.model
                .DeviceRegistrationResult.Success -> {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isBackendLoginCompleted = true,
                            isDeviceRegistrationCompleted = true,
                            isLoginCompleted = true,
                            errorMessage = null,
                            infoMessage = (
                                    result.message
                                        ?: (
                                                "Authenticator cihazı "
                                                        + "başarıyla "
                                                        + "kaydedildi."
                                                )
                                    ),
                            failedStage = null,
                            canRetryDeviceRegistration = false,
                            devicePublicId =
                            result.device.publicId,
                            deviceRegistrationMessage =
                            result.message,
                        )
                }

                is com.alperensarac
                .projectmanagementauthenticator
                .data.remote.model
                .DeviceRegistrationResult.Failure -> {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isBackendLoginCompleted = true,
                            isDeviceRegistrationCompleted = false,
                            isLoginCompleted = false,
                            errorMessage = result.message,
                            infoMessage = null,
                            failedStage =
                            AuthenticatorLoginStage
                                .DEVICE_REGISTRATION,
                            canRetryDeviceRegistration = true,
                        )
                }
            }
        }
    }


    /*
     * =====================================================
     * STATE TEMİZLEME
     * =====================================================
     */


    /**
     * Kullanıcı hata mesajını gördükten sonra temizlemek
     * için çağrılabilir.
     *
     * Snackbar veya AlertDialog kapatıldığında
     * kullanılabilir.
     */
    fun clearErrorMessage() {
        _uiState.value =
            _uiState.value.copy(
                errorMessage = null,
            )
    }


    /**
     * Bilgi mesajını temizler.
     */
    fun clearInfoMessage() {
        _uiState.value =
            _uiState.value.copy(
                infoMessage = null,
            )
    }


    /**
     * Login işlemi tamamlandıktan sonra navigation olayı
     * tüketildiğinde çağrılır.
     *
     * Böylece Activity yeniden oluşturulduğunda aynı
     * navigation işleminin tekrar tetiklenmesi önlenir.
     */
    fun consumeLoginCompletedEvent() {
        _uiState.value =
            _uiState.value.copy(
                isLoginCompleted = false,
            )
    }


    /**
     * Login formunu başlangıç durumuna döndürür.
     */
    fun resetLoginForm() {
        _uiState.value =
            LoginUiState()
    }


    /*
     * =====================================================
     * SONUÇ YÖNETİMİ
     * =====================================================
     */


    /**
     * Başarılı login ve cihaz kayıt sonucunu UI state
     * içerisine aktarır.
     */
    private fun handleLoginSuccess(
        result: AuthenticatorLoginResult.Success,
    ) {
        val loginResult =
            result.login

        val registrationResult =
            result.deviceRegistration


        _uiState.value =
            _uiState.value.copy(
                /*
                 * Şifre başarılı login sonrasında state
                 * içinden temizlenir.
                 */
                password = "",

                isLoading = false,

                isBackendLoginCompleted = true,

                isDeviceRegistrationCompleted = true,

                isLoginCompleted = true,

                errorMessage = null,

                infoMessage = (
                        registrationResult.message
                            ?: (
                                    "Giriş ve cihaz kaydı "
                                            + "başarıyla tamamlandı."
                                    )
                        ),

                failedStage = null,

                canRetryDeviceRegistration = false,

                displayName =
                loginResult.displayName,

                authenticatedEmail =
                loginResult.email,

                devicePublicId =
                registrationResult
                    .device
                    .publicId,

                deviceRegistrationMessage =
                registrationResult.message,
            )
    }


    /**
     * Login veya cihaz kayıt aşamasındaki hata sonucunu
     * UI state içerisine aktarır.
     */
    private fun handleLoginFailure(
        result: AuthenticatorLoginResult.Failure,
    ) {
        val backendLoginCompleted =
            result.loginSucceeded

        val canRetryRegistration =
            (
                    result.loginSucceeded &&
                            result.stage ==
                            AuthenticatorLoginStage
                                .DEVICE_REGISTRATION
                    )


        _uiState.value =
            _uiState.value.copy(
                /*
                 * Login başarısızsa şifreyi state içinde
                 * tutmuyoruz.
                 *
                 * Cihaz kaydı başarısızsa .NET login
                 * başarılı olduğu için kullanıcı yeniden
                 * şifre girmeden kayıt deneyebilir.
                 */
                password =
                if (backendLoginCompleted) {
                    ""
                } else {
                    ""
                },

                isLoading = false,

                isBackendLoginCompleted =
                backendLoginCompleted,

                isDeviceRegistrationCompleted = false,

                isLoginCompleted = false,

                errorMessage = result.message,

                infoMessage =
                when (result.stage) {
                    AuthenticatorLoginStage.LOGIN -> {
                        null
                    }

                    AuthenticatorLoginStage
                        .DEVICE_REGISTRATION -> {

                        if (result.loginSucceeded) {
                            (
                                    "Project Management "
                                            + "girişi başarılı, "
                                            + "ancak cihaz kaydı "
                                            + "tamamlanamadı."
                                    )
                        } else {
                            null
                        }
                    }
                },

                failedStage = result.stage,

                canRetryDeviceRegistration =
                canRetryRegistration,
            )
    }


    /*
     * =====================================================
     * FORM DOĞRULAMA
     * =====================================================
     */


    /**
     * Login formunun temel yerel doğrulamasını yapar.
     *
     * Geçerliyse null, geçersizse hata mesajı döndürür.
     */
    private fun validateLoginForm(
        email: String,
        password: String,
    ): String? {
        if (email.isBlank()) {
            return "E-posta adresi boş olamaz."
        }

        if (!email.contains("@")) {
            return "Geçerli bir e-posta adresi giriniz."
        }

        if (password.isBlank()) {
            return "Şifre boş olamaz."
        }

        return null
    }
}


/*
 * =========================================================
 * VIEWMODEL FACTORY
 * =========================================================
 */


/**
 * LoginViewModel constructor içerisinde AuthRepository
 * aldığı için varsayılan ViewModel oluşturucusu yeterli
 * değildir.
 *
 * Bu Factory, AuthRepository nesnesini ViewModel'e verir.
 *
 * Activity içerisinde örnek kullanım:
 *
 * private val viewModel: LoginViewModel by viewModels {
 *     LoginViewModelFactory(
 *         authRepository = authRepository,
 *     )
 * }
 */
class LoginViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                LoginViewModel::class.java,
            )
        ) {
            return LoginViewModel(
                authRepository = authRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Bilinmeyen ViewModel sınıfı: "
                    + modelClass.name,
        )
    }
}