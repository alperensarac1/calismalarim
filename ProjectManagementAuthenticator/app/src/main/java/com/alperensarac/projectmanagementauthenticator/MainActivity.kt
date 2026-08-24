package com.alperensarac.projectmanagementauthenticator

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.alperensarac.projectmanagementauthenticator.data.local.AuthSessionManager
import com.alperensarac.projectmanagementauthenticator.data.repository.AuthRepository
import com.alperensarac.projectmanagementauthenticator.databinding.ActivityMainBinding
import com.alperensarac.projectmanagementauthenticator.presentation.authenticator.AuthenticatorActivity
import com.alperensarac.projectmanagementauthenticator.presentation.login.LoginUiState
import com.alperensarac.projectmanagementauthenticator.presentation.login.LoginViewModel
import com.alperensarac.projectmanagementauthenticator.presentation.login.LoginViewModelFactory

import com.google.android.material.snackbar.Snackbar

import kotlinx.coroutines.launch


/*
 * =========================================================
 * MAIN ACTIVITY
 * =========================================================
 */


/**
 * Uygulamanın giriş ve cihaz kayıt ekranıdır.
 *
 * Bu Activity içerisindeki temel akış:
 *
 * 1. Kullanıcı e-posta ve şifresini girer.
 * 2. Bilgiler LoginViewModel'e aktarılır.
 * 3. ViewModel, AuthRepository üzerinden .NET backend
 *    login işlemini gerçekleştirir.
 * 4. Login başarılı olursa Android cihazı Python
 *    Authenticator servisine kaydedilir.
 * 5. Cihaz kaydı tamamlanınca AuthenticatorActivity
 *    ekranına geçilir.
 *
 * Daha önce tamamlanmış bir cihaz oturumu bulunuyorsa
 * login ekranı gösterilmeden doğrudan Authenticator
 * ekranına geçilir.
 */
class MainActivity : AppCompatActivity() {

    /*
     * =====================================================
     * VIEW BINDING
     * =====================================================
     */


    /**
     * activity_main.xml içerisindeki bileşenlere
     * findViewById kullanmadan erişmemizi sağlar.
     */
    private lateinit var binding:
            ActivityMainBinding


    /*
     * =====================================================
     * BAĞIMLILIKLAR
     * =====================================================
     */


    /**
     * DataStore tabanlı oturum yöneticisidir.
     *
     * Şunları saklar:
     *
     * - .NET access token
     * - .NET refresh token
     * - Device access token
     * - Device public ID
     * - Installation ID
     * - Kullanıcı bilgileri
     */
    private val authSessionManager:
            AuthSessionManager by lazy {

        AuthSessionManager(
            context = applicationContext,
        )
    }


    /**
     * Login ve cihaz kayıt işlemlerini yöneten
     * Repository sınıfıdır.
     */
    private val authRepository:
            AuthRepository by lazy {

        AuthRepository(
            authSessionManager =
            authSessionManager,
        )
    }


    /**
     * Login ekranının ViewModel nesnesidir.
     *
     * Constructor parametresi aldığı için özel Factory
     * kullanılarak oluşturulur.
     */
    private val loginViewModel:
            LoginViewModel by viewModels {

        LoginViewModelFactory(
            authRepository =
            authRepository,
        )
    }


    /*
     * =====================================================
     * NAVIGATION KONTROLÜ
     * =====================================================
     */


    /**
     * Aynı state birden fazla kez yayınlandığında
     * AuthenticatorActivity ekranının tekrar tekrar
     * açılmasını engeller.
     */
    private var hasNavigatedToAuthenticator =
        false


    /*
     * =====================================================
     * ACTIVITY YAŞAM DÖNGÜSÜ
     * =====================================================
     */


    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(
            savedInstanceState,
        )


        binding =
            ActivityMainBinding.inflate(
                layoutInflater,
            )


        setContentView(
            binding.root,
        )


        setupInputListeners()

        setupButtonListeners()

        setupKeyboardActions()

        setupBackPressHandling()

        observeLoginUiState()


        /*
         * Uygulama daha önce kayıtlı ve geçerli bir
         * Authenticator oturumu oluşturduysa doğrudan
         * Authenticator ana ekranına geçilir.
         */
        checkExistingSession()
    }


    /*
     * =====================================================
     * INPUT LISTENERLARI
     * =====================================================
     */


    /**
     * E-posta ve şifre alanlarındaki değişiklikleri
     * ViewModel'e aktarır.
     */
    private fun setupInputListeners() {
        binding.emailEditText
            .doAfterTextChanged {
                    editable ->

                loginViewModel.onEmailChanged(
                    value =
                    editable
                        ?.toString()
                        .orEmpty(),
                )
            }


        binding.passwordEditText
            .doAfterTextChanged {
                    editable ->

                loginViewModel.onPasswordChanged(
                    value =
                    editable
                        ?.toString()
                        .orEmpty(),
                )
            }
    }


    /*
     * =====================================================
     * BUTON LISTENERLARI
     * =====================================================
     */


    /**
     * Giriş ve cihaz kayıt tekrar deneme butonlarının
     * click işlemlerini bağlar.
     */
    private fun setupButtonListeners() {
        binding.loginButton
            .setOnClickListener {

                hideKeyboard()

                loginViewModel.login()
            }


        binding.retryDeviceRegistrationButton
            .setOnClickListener {

                hideKeyboard()

                loginViewModel
                    .retryDeviceRegistration()
            }
    }


    /*
     * =====================================================
     * KLAVYE İŞLEMLERİ
     * =====================================================
     */


    /**
     * Kullanıcı şifre alanındayken klavyedeki Done
     * butonuna bastığında login işlemini başlatır.
     */
    private fun setupKeyboardActions() {
        binding.passwordEditText
            .setOnEditorActionListener {
                    _,
                    actionId,
                    _ ->

                val shouldSubmit =
                    actionId ==
                            EditorInfo.IME_ACTION_DONE


                if (shouldSubmit) {
                    hideKeyboard()

                    loginViewModel.login()
                }


                shouldSubmit
            }
    }


    /**
     * Açık olan yazılım klavyesini kapatır.
     */
    private fun hideKeyboard() {
        val focusedView =
            currentFocus


        if (focusedView != null) {
            val inputMethodManager =
                getSystemService<InputMethodManager>()


            inputMethodManager
                ?.hideSoftInputFromWindow(
                    focusedView.windowToken,
                    0,
                )


            focusedView.clearFocus()
        }
    }


    /*
     * =====================================================
     * STATE GÖZLEMLEME
     * =====================================================
     */


    /**
     * LoginViewModel tarafından yayınlanan UI state
     * değerini lifecycle-aware biçimde dinler.
     *
     * Activity STARTED durumunun altına geçtiğinde
     * collect işlemi otomatik olarak durur.
     */
    private fun observeLoginUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                loginViewModel
                    .uiState
                    .collect {
                            uiState ->

                        renderLoginUiState(
                            uiState =
                            uiState,
                        )
                    }
            }
        }
    }


    /*
     * =====================================================
     * EKRANI STATE'E GÖRE GÜNCELLEME
     * =====================================================
     */


    /**
     * Güncel LoginUiState değerini XML bileşenlerine
     * aktarır.
     */
    private fun renderLoginUiState(
        uiState: LoginUiState,
    ) {
        updateEmailField(
            email =
            uiState.email,
        )


        updatePasswordField(
            password =
            uiState.password,
        )


        /*
         * İşlem devam ederken kullanıcının formu
         * değiştirmesini engelliyoruz.
         */
        binding.emailInputLayout.isEnabled =
            !uiState.isLoading

        binding.passwordInputLayout.isEnabled =
            !uiState.isLoading


        binding.loginButton.isEnabled =
            uiState.isLoginButtonEnabled


        binding.loginButton.text =
            if (uiState.isLoading) {
                "İşlem Devam Ediyor..."
            } else {
                "Giriş Yap ve Cihazı Kaydet"
            }


        binding.loadingContainer.isVisible =
            uiState.isLoading


        renderInfoMessage(
            message =
            uiState.infoMessage,
        )


        renderErrorMessage(
            message =
            uiState.errorMessage,
        )


        /*
         * .NET login başarılı fakat Python cihaz kaydı
         * başarısız olmuşsa tekrar deneme butonu
         * gösterilir.
         */
        binding.retryDeviceRegistrationButton
            .isVisible =
            uiState.canRetryDeviceRegistration


        binding.retryDeviceRegistrationButton
            .isEnabled =
            uiState.isRetryDeviceButtonEnabled


        /*
         * Login ve cihaz kayıt işlemi tamamlandıysa
         * Authenticator ekranına geçilir.
         */
        if (uiState.isLoginCompleted) {
            handleLoginCompleted(
                uiState =
                uiState,
            )
        }
    }


    /**
     * State içerisindeki e-posta değerini EditText ile
     * eşitler.
     *
     * Değer zaten aynıysa setText çağrılmaz. Böylece
     * kullanıcının cursor konumu bozulmaz.
     */
    private fun updateEmailField(
        email: String,
    ) {
        val currentValue =
            binding.emailEditText
                .text
                ?.toString()
                .orEmpty()


        if (currentValue == email) {
            return
        }


        binding.emailEditText.setText(
            email,
        )


        binding.emailEditText.setSelection(
            email.length,
        )
    }


    /**
     * State içerisindeki şifre değerini EditText ile
     * eşitler.
     *
     * Başarılı login sonrasında ViewModel şifreyi
     * temizlediğinde ekrandaki şifre alanı da temizlenir.
     */
    private fun updatePasswordField(
        password: String,
    ) {
        val currentValue =
            binding.passwordEditText
                .text
                ?.toString()
                .orEmpty()


        if (currentValue == password) {
            return
        }


        binding.passwordEditText.setText(
            password,
        )


        binding.passwordEditText.setSelection(
            password.length,
        )
    }


    /**
     * Bilgi mesajını gösterir veya gizler.
     */
    private fun renderInfoMessage(
        message: String?,
    ) {
        val normalizedMessage =
            message
                ?.trim()
                .orEmpty()


        binding.infoMessageTextView.isVisible =
            normalizedMessage.isNotBlank()


        binding.infoMessageTextView.text =
            normalizedMessage
    }


    /**
     * Hata mesajını gösterir veya gizler.
     */
    private fun renderErrorMessage(
        message: String?,
    ) {
        val normalizedMessage =
            message
                ?.trim()
                .orEmpty()


        binding.errorMessageTextView.isVisible =
            normalizedMessage.isNotBlank()


        binding.errorMessageTextView.text =
            normalizedMessage
    }


    /*
     * =====================================================
     * BAŞARILI LOGIN
     * =====================================================
     */


    /**
     * .NET login ve Python cihaz kayıt işlemi başarıyla
     * tamamlandığında çağrılır.
     */
    private fun handleLoginCompleted(
        uiState: LoginUiState,
    ) {
        /*
         * StateFlow aynı state'i tekrar yayınlarsa ikinci
         * Activity açılmasını engelliyoruz.
         */
        if (hasNavigatedToAuthenticator) {
            return
        }


        hasNavigatedToAuthenticator =
            true


        /*
         * Navigation olayı ViewModel tarafında
         * tüketilir.
         */
        loginViewModel
            .consumeLoginCompletedEvent()


        val displayName =
            uiState.displayName
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        val message =
            if (displayName != null) {
                "$displayName, cihazınız başarıyla kaydedildi."
            } else {
                "Giriş ve cihaz kaydı başarıyla tamamlandı."
            }


        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT,
        ).show()


        openAuthenticatorActivity()
    }


    /*
     * =====================================================
     * MEVCUT OTURUM KONTROLÜ
     * =====================================================
     */


    /**
     * Uygulama açıldığında DataStore içinde daha önce
     * oluşturulmuş tam bir Authenticator oturumu olup
     * olmadığını kontrol eder.
     *
     * Aşağıdaki değerlerin bulunması beklenir:
     *
     * - .NET access token
     * - Device access token
     * - Device public ID
     * - Installation ID
     *
     * Oturum varsa login ekranı atlanır.
     */
    private fun checkExistingSession() {
        lifecycleScope.launch {
            val hasCompleteSession =
                try {
                    authRepository
                        .hasCompleteAuthenticatorSession()
                } catch (_: Exception) {
                    false
                }


            if (!hasCompleteSession) {
                return@launch
            }


            if (hasNavigatedToAuthenticator) {
                return@launch
            }


            hasNavigatedToAuthenticator =
                true


            val session =
                try {
                    authRepository
                        .getCurrentSession()
                } catch (_: Exception) {
                    null
                }


            val displayName =
                session
                    ?.displayName
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }


            val message =
                if (displayName != null) {
                    "$displayName için kayıtlı cihaz oturumu açılıyor."
                } else {
                    "Kayıtlı Authenticator oturumu açılıyor."
                }


            Snackbar.make(
                binding.root,
                message,
                Snackbar.LENGTH_SHORT,
            ).show()


            openAuthenticatorActivity()
        }
    }


    /*
     * =====================================================
     * AUTHENTICATOR EKRANINA GEÇİŞ
     * =====================================================
     */


    /**
     * AuthenticatorActivity ekranını açar.
     *
     * MainActivity finish edildiği için kullanıcı geri
     * tuşuna bastığında login ekranına dönmez.
     *
     * Kullanıcının yeniden login ekranına dönmesi için
     * ileride AuthenticatorActivity içerisine logout
     * butonu ekleyeceğiz.
     */
    private fun openAuthenticatorActivity() {
        val intent =
            Intent(
                this,
                AuthenticatorActivity::class.java,
            )


        startActivity(
            intent,
        )


        finish()
    }


    /*
     * =====================================================
     * GERİ TUŞU YÖNETİMİ
     * =====================================================
     */


    /**
     * Login veya cihaz kayıt işlemi devam ederken
     * kullanıcının Activity'yi yanlışlıkla kapatmasını
     * engeller.
     */
    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(
                true,
            ) {
                override fun handleOnBackPressed() {
                    val currentState =
                        loginViewModel
                            .uiState
                            .value


                    if (currentState.isLoading) {
                        Snackbar.make(
                            binding.root,
                            "İşlem devam ederken uygulamadan çıkılamaz.",
                            Snackbar.LENGTH_SHORT,
                        ).show()

                        return
                    }


                    /*
                     * Callback geçici olarak kapatılır ve
                     * sistemin varsayılan geri davranışı
                     * çalıştırılır.
                     */
                    isEnabled =
                        false


                    onBackPressedDispatcher
                        .onBackPressed()
                }
            },
        )
    }
}