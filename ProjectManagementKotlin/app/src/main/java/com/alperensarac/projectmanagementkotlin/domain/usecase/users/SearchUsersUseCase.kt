package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Projeye eklenecek kullanıcıları aramak için kullanılır.
 *
 * Buradaki amaç normal Admin kullanıcı listesinden biraz farklıdır:
 *
 * - Kullanıcı adına / e-postasına göre arama yapar.
 * - Rol filtresi uygulamaz.
 * - Yalnızca aktif kullanıcıları getirir.
 *
 * Böylece pasif kullanıcıların projeye yanlışlıkla eklenmesini
 * UI tarafında engellemiş oluruz.
 *
 * NOT:
 * Asıl yetki ve iş kuralı kontrolü yine backend tarafında kalmalıdır.
 */
class SearchUsersUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {

    operator fun invoke(
        search: String
    ): Flow<PagingData<User>> {

        /*
         * UsersRepository artık ayrı ayrı search / role / isActive
         * parametreleri almıyor.
         *
         * Bütün filtreleri tek bir UserFilter domain modeli üzerinden
         * kabul ediyor.
         */
        val filter =
            UserFilter(
                search = search.trim(),

                /*
                 * Belirli bir sistem rolüne göre filtrelemiyoruz.
                 */
                role = null,

                /*
                 * Projeye kullanıcı ekleme ekranında yalnızca
                 * aktif hesapları göstermek istiyoruz.
                 */
                isActive = true
            )

        return usersRepository.getUsers(
            filter = filter
        )
    }
}