package com.example.onlinequizjetpack.model

/*
    AppScreen, uygulamadaki ekranları temsil eden sealed class yapısıdır.

    Jetpack Compose tarafında Fragment veya XML kullanmadığımız için
    ekran geçişlerini bu sınıf üzerinden yönetiyoruz.

    Örneğin:
    - Kullanıcı ana ekrandaysa: AppScreen.Home
    - Oda oluşturma ekranındaysa: AppScreen.CreateRoom
    - Oda sahibi panelindeyse: AppScreen.OwnerRoom(...)
    - Quiz ekranındaysa: AppScreen.Quiz(...)

    ViewModel içindeki currentScreen değeri değiştiğinde
    LiveQuizApp.kt bu değere bakarak hangi Composable ekranın çizileceğine karar verir.
*/

sealed class AppScreen {

    /*
        Ana ekran.

        Burada kullanıcı:
        - Oda oluştur
        - Odaya giriş yap

        seçeneklerini görür.
    */
    data object Home : AppScreen()

    /*
        Oda oluşturma ekranı.

        Kullanıcı:
        - kullanıcı adı
        - soru süresi

        bilgilerini girer.
    */
    data object CreateRoom : AppScreen()

    /*
        Odaya katılma ekranı.

        Kullanıcı:
        - kullanıcı adı
        - oda kodu

        bilgilerini girer.
    */
    data object JoinRoom : AppScreen()

    /*
        Oda sahibi ekranı.

        Bu ekranda oda sahibi:
        - oda kodunu görür
        - oyuncu listesini görür
        - soru ekler
        - şık ekler/siler
        - quizi başlatır

        Parametreler:
        roomCode:
            Python server tarafından üretilen oda kodu.

        username:
            Oda sahibinin kullanıcı adı.

        questionTime:
            Her soru için belirlenen süre.
    */
    data class OwnerRoom(
        val roomCode: String,
        val username: String,
        val questionTime: Int
    ) : AppScreen()

    /*
        Bekleme odası ekranı.

        Normal kullanıcı odaya katılınca bu ekranda bekler.
        Oda sahibi quizi başlatınca Quiz ekranına geçilir.
    */
    data class WaitingRoom(
        val roomCode: String,
        val username: String,
        val questionTime: Int
    ) : AppScreen()

    /*
        Quiz ekranı.

        Bu ekranda:
        - soru görünür
        - şıklar görünür
        - süre görünür
        - kullanıcı cevap verir
        - puan tablosu görünür

        isOwner:
            Bu kullanıcı oda sahibi mi?
            Bazı ek özelliklerde kullanmak için tutulur.
    */
    data class Quiz(
        val roomCode: String,
        val username: String,
        val questionTime: Int,
        val isOwner: Boolean
    ) : AppScreen()

    /*
        Kazanan ekranı.

        Quiz bitince herkesin ekranında açılır.
        Server'dan gelen winners ve scoreboard JSON metinleri burada tutulur.
    */
    data class Winner(
        val winnersJson: String,
        val scoreboardJson: String
    ) : AppScreen()
}