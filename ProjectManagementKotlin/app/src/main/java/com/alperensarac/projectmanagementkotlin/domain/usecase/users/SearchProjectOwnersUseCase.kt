package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import androidx.paging.PagingData
import androidx.paging.filter
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Proje sahibi olarak seçilebilecek kullanıcıları getirir.
 *
 * Proje owner'ı için Android tarafında istediğimiz kurallar:
 *
 * 1. Kullanıcı aktif olmalı.
 * 2. Sistem rolü Admin veya ProjectManager olmalı.
 *
 * TeamMember kullanıcı proje sahibi seçim ekranında gösterilmez.
 *
 * ÖNEMLİ:
 *
 * UserFilter şu anda tek bir role değeri destekliyor.
 *
 * Biz ise:
 *
 * Admin OR ProjectManager
 *
 * şeklinde iki farklı rolü aynı anda kabul etmek istiyoruz.
 *
 * Bundan dolayı:
 *
 * - backend sorgusunda isActive = true uygularız
 * - role filtresini null göndeririz
 * - gelen PagingData üzerinde Admin / ProjectManager filtresi uygularız
 *
 * Asıl güvenlik ve iş kuralı yine backend tarafında kalmalıdır.
 */
class SearchProjectOwnersUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {

    operator fun invoke(
        search: String
    ): Flow<PagingData<User>> {

        // ---------------------------------------------------------------------
        // BACKEND FILTER
        // ---------------------------------------------------------------------

        val filter =
            UserFilter(

                search =
                search.trim(),

                /*
                 * Repository tek rol kabul ettiği için burada role seçmiyoruz.
                 *
                 * Admin OR ProjectManager kontrolünü aşağıdaki PagingData
                 * filtresinde uygulayacağız.
                 */
                role =
                null,

                /*
                 * Pasif hesapları backend seviyesinde mümkün olduğunca
                 * erkenden eliyoruz.
                 */
                isActive =
                true
            )

        // ---------------------------------------------------------------------
        // PAGING
        // ---------------------------------------------------------------------

        return usersRepository
            .getUsers(
                filter = filter
            )
            .map { pagingData ->

                pagingData.filter { user ->

                    /*
                     * User modelinde isActive olmasına rağmen backend sorgusunda
                     * zaten isActive=true gönderiyoruz.
                     *
                     * Burada ikinci kez kontrol etmek savunmacı bir önlemdir.
                     */
                    user.isActive &&
                            isAllowedOwnerRole(
                                role = user.role
                            )
                }
            }
    }

    // =========================================================================
    // OWNER ROLE
    // =========================================================================

    /**
     * Bir kullanıcının proje owner'ı seçim listesinde
     * gösterilip gösterilmeyeceğini belirler.
     */
    private fun isAllowedOwnerRole(
        role: String
    ): Boolean {

        return role.equals(
            ADMIN_ROLE,
            ignoreCase = true
        ) ||
                role.equals(
                    PROJECT_MANAGER_ROLE,
                    ignoreCase = true
                )
    }

    private companion object {

        const val ADMIN_ROLE =
            "Admin"

        const val PROJECT_MANAGER_ROLE =
            "ProjectManager"
    }
}