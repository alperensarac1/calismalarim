package com.alperensarac.projectmanagementkotlin.core.auth.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Oturumla ilgili global ve tek seferlik olayları yayınlar.
 *
 * MainActivity bu akışı dinleyerek Login ekranına güvenli biçimde
 * yönlendirme yapar.
 */
@Singleton
class SessionEventBus @Inject constructor() {

    /**
     * Activity kısa süreliğine STARTED durumunda değilse son bir olayın
     * buffer içerisinde tutulabilmesi için extraBufferCapacity kullanılır.
     */
    private val mutableEvents = MutableSharedFlow<SessionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Dış katmanlar olayları okuyabilir ancak doğrudan MutableSharedFlow
     * nesnesine erişemez.
     */
    val events: SharedFlow<SessionEvent> =
        mutableEvents.asSharedFlow()

    /**
     * Token yenileme başarısız olduğunda kullanılır.
     */
    fun notifySessionExpired() {
        mutableEvents.tryEmit(
            SessionEvent.SessionExpired
        )
    }

    /**
     * Kullanıcı bilinçli olarak çıkış yaptığında kullanılır.
     */
    fun notifyUserLoggedOut() {
        mutableEvents.tryEmit(
            SessionEvent.UserLoggedOut
        )
    }
}