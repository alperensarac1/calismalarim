package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemTaskHistoryBinding
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory

/**
 * Görev geçmişini timeline şeklinde gösterir.
 *
 * Backend history kayıtlarında teknik enum değerleri gelir:
 *
 * StatusChanged
 * PriorityChanged
 * AssignedUserChanged
 * Updated
 *
 * UI katmanında bunları kullanıcı dostu metinlere dönüştürüyoruz.
 *
 * Aynı şekilde:
 *
 * Todo       -> Yapılacak
 * InProgress -> Devam Ediyor
 * High       -> Yüksek
 *
 * gibi domain/API değerleri ekranda ham olarak gösterilmez.
 *
 * ÖNEMLİ:
 *
 * Backend tarafından bilinmeyen yeni bir changeType gönderilirse
 * adapter crash olmaz. Raw değer fallback olarak gösterilir.
 */
class TaskHistoryAdapter(
    private val dateTimeFormatter: DateTimeFormatter
) : ListAdapter<
        TaskHistory,
        TaskHistoryAdapter.HistoryViewHolder
        >(
    DiffCallback
) {

    // =========================================================================
    // CREATE VIEW HOLDER
    // =========================================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val binding =
            ItemTaskHistoryBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return HistoryViewHolder(
            binding
        )
    }

    // =========================================================================
    // BIND
    // =========================================================================

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        holder.bind(
            item = getItem(position),
            isLastItem =
            position == itemCount - 1
        )
    }

    // =========================================================================
    // VIEW HOLDER
    // =========================================================================

    inner class HistoryViewHolder(
        private val binding:
        ItemTaskHistoryBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: TaskHistory,
            isLastItem: Boolean
        ) {

            val context =
                binding.root.context

            // -----------------------------------------------------------------
            // CHANGE TYPE
            // -----------------------------------------------------------------

            /*
             * Backend enum değerini doğrudan kullanıcıya göstermiyoruz.
             *
             * Örneğin:
             *
             * StatusChanged
             *
             * yerine:
             *
             * Durum değiştirildi
             */
            binding.textViewHistoryChangeType
                .text =
                getChangeTypeDisplayName(
                    context = context,
                    changeType = item.changeType
                )

            // -----------------------------------------------------------------
            // DESCRIPTION
            // -----------------------------------------------------------------

            binding.textViewHistoryDescription
                .text =
                item.description
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: context.getString(
                        R.string.task_history_no_description
                    )

            // -----------------------------------------------------------------
            // VALUE CHANGE
            // -----------------------------------------------------------------

            renderValueChange(
                context = context,
                item = item
            )

            // -----------------------------------------------------------------
            // USER
            // -----------------------------------------------------------------

            binding.textViewHistoryUser
                .text =
                item.changedByUserFullName

            binding.textViewHistoryEmail
                .text =
                item.changedByUserEmail

            // -----------------------------------------------------------------
            // DATE
            // -----------------------------------------------------------------

            binding.textViewHistoryCreatedAt
                .text =
                dateTimeFormatter
                    .formatUtcDateTime(
                        item.createdAtUtc
                    )

            // -----------------------------------------------------------------
            // TIMELINE LINE
            // -----------------------------------------------------------------

            /*
             * Timeline'daki son item'ın altında devam eden çizgi
             * göstermiyoruz.
             */
            binding.viewHistoryLine
                .isVisible =
                !isLastItem
        }

        // =====================================================================
        // VALUE CHANGE
        // =====================================================================

        /**
         * History kaydındaki oldValue/newValue alanlarını change type'a
         * göre kullanıcı dostu hale getirir.
         *
         * Önemli örnek:
         *
         * Backend:
         *
         * StatusChanged
         * Todo -> InProgress
         *
         * UI:
         *
         * Yapılacak -> Devam Ediyor
         */
        private fun renderValueChange(
            context: Context,
            item: TaskHistory
        ) {

            val normalizedChangeType =
                item.changeType.trim()

            // -----------------------------------------------------------------
            // ASSIGNMENT
            // -----------------------------------------------------------------

            /*
             * AssignedUserChanged için backend OldValue / NewValue
             * alanlarında UserId tutuyor.
             *
             * Örnek:
             *
             * 12 -> 18
             *
             * Bu değer kullanıcı açısından anlamlı olmadığı için
             * timeline'da göstermiyoruz.
             *
             * Bunun yerine backend description:
             *
             * "Görev başka bir kullanıcıya atandı."
             *
             * gibi kullanıcı dostu açıklamayı zaten gönderiyor.
             */
            if (
                normalizedChangeType.equals(
                    CHANGE_TYPE_ASSIGNED_USER_CHANGED,
                    ignoreCase = true
                )
            ) {

                binding.textViewHistoryValueChange
                    .isVisible =
                    false

                binding.textViewHistoryValueChange
                    .text =
                    ""

                return
            }

            // -----------------------------------------------------------------
            // GENERAL UPDATE
            // -----------------------------------------------------------------

            /*
             * Genel Updated history'sinde backend old/new value
             * göndermiyor.
             */
            if (
                normalizedChangeType.equals(
                    CHANGE_TYPE_UPDATED,
                    ignoreCase = true
                )
            ) {

                binding.textViewHistoryValueChange
                    .isVisible =
                    false

                binding.textViewHistoryValueChange
                    .text =
                    ""

                return
            }

            // -----------------------------------------------------------------
            // NORMALIZE OLD VALUE
            // -----------------------------------------------------------------

            val oldValue =
                item.oldValue
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { value ->

                        formatHistoryValue(
                            context = context,
                            changeType =
                            normalizedChangeType,
                            value = value
                        )
                    }

            // -----------------------------------------------------------------
            // NORMALIZE NEW VALUE
            // -----------------------------------------------------------------

            val newValue =
                item.newValue
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { value ->

                        formatHistoryValue(
                            context = context,
                            changeType =
                            normalizedChangeType,
                            value = value
                        )
                    }

            // -----------------------------------------------------------------
            // VISIBILITY
            // -----------------------------------------------------------------

            binding.textViewHistoryValueChange
                .isVisible =
                oldValue != null ||
                        newValue != null

            // -----------------------------------------------------------------
            // TEXT
            // -----------------------------------------------------------------

            binding.textViewHistoryValueChange
                .text =
                when {

                    oldValue != null &&
                            newValue != null -> {

                        context.getString(
                            R.string.task_history_old_new_format,
                            oldValue,
                            newValue
                        )
                    }

                    newValue != null -> {

                        context.getString(
                            R.string.task_history_new_value_format,
                            newValue
                        )
                    }

                    oldValue != null -> {

                        context.getString(
                            R.string.task_history_old_value_format,
                            oldValue
                        )
                    }

                    else ->
                        ""
                }
        }
    }

    // =========================================================================
    // CHANGE TYPE DISPLAY
    // =========================================================================

    /**
     * Backend'in teknik TaskChangeType değerlerini
     * kullanıcı dostu başlıklara dönüştürür.
     *
     * Bilinmeyen değerlerde raw value gösterilir.
     *
     * Bu özellikle backend'e ileride yeni bir enum eklenirse
     * eski Android uygulamasının crash olmaması için önemlidir.
     */
    private fun getChangeTypeDisplayName(
        context: Context,
        changeType: String
    ): String {

        return when {

            changeType.equals(
                CHANGE_TYPE_STATUS_CHANGED,
                ignoreCase = true
            ) -> {

                "Durum değiştirildi"
            }

            changeType.equals(
                CHANGE_TYPE_PRIORITY_CHANGED,
                ignoreCase = true
            ) -> {

                "Öncelik değiştirildi"
            }

            changeType.equals(
                CHANGE_TYPE_ASSIGNED_USER_CHANGED,
                ignoreCase = true
            ) -> {

                "Görev ataması değiştirildi"
            }

            changeType.equals(
                CHANGE_TYPE_UPDATED,
                ignoreCase = true
            ) -> {

                "Görev bilgileri güncellendi"
            }

            changeType.isBlank() -> {

                context.getString(
                    R.string.task_history_no_description
                )
            }

            else -> {

                /*
                 * Backend ileride yeni bir history tipi eklerse
                 * en azından değer kaybolmaz.
                 */
                changeType
            }
        }
    }

    // =========================================================================
    // VALUE FORMATTER
    // =========================================================================

    /**
     * oldValue / newValue alanlarını changeType'a göre dönüştürür.
     */
    private fun formatHistoryValue(
        context: Context,
        changeType: String,
        value: String
    ): String {

        return when {

            // -----------------------------------------------------------------
            // STATUS
            // -----------------------------------------------------------------

            changeType.equals(
                CHANGE_TYPE_STATUS_CHANGED,
                ignoreCase = true
            ) -> {

                getStatusDisplayName(
                    context = context,
                    value = value
                )
            }

            // -----------------------------------------------------------------
            // PRIORITY
            // -----------------------------------------------------------------

            changeType.equals(
                CHANGE_TYPE_PRIORITY_CHANGED,
                ignoreCase = true
            ) -> {

                getPriorityDisplayName(
                    context = context,
                    value = value
                )
            }

            // -----------------------------------------------------------------
            // FALLBACK
            // -----------------------------------------------------------------

            else ->
                value
        }
    }

    // =========================================================================
    // STATUS DISPLAY
    // =========================================================================

    /**
     * Backend ProjectTaskStatus enum değerlerini Türkçeleştirir.
     */
    private fun getStatusDisplayName(
        context: Context,
        value: String
    ): String {

        return when {

            value.equals(
                STATUS_TODO,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_status_todo
                )
            }

            value.equals(
                STATUS_IN_PROGRESS,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_status_in_progress
                )
            }

            value.equals(
                STATUS_IN_REVIEW,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_status_in_review
                )
            }

            value.equals(
                STATUS_DONE,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_status_done
                )
            }

            else ->
                value
        }
    }

    // =========================================================================
    // PRIORITY DISPLAY
    // =========================================================================

    /**
     * Backend TaskPriority enum değerlerini Türkçeleştirir.
     */
    private fun getPriorityDisplayName(
        context: Context,
        value: String
    ): String {

        return when {

            value.equals(
                PRIORITY_LOW,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_priority_low
                )
            }

            value.equals(
                PRIORITY_MEDIUM,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_priority_medium
                )
            }

            value.equals(
                PRIORITY_HIGH,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_priority_high
                )
            }

            value.equals(
                PRIORITY_CRITICAL,
                ignoreCase = true
            ) -> {

                context.getString(
                    R.string.tasks_priority_critical
                )
            }

            else ->
                value
        }
    }

    // =========================================================================
    // DIFF CALLBACK
    // =========================================================================

    private object DiffCallback :
        DiffUtil.ItemCallback<TaskHistory>() {

        override fun areItemsTheSame(
            oldItem: TaskHistory,
            newItem: TaskHistory
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskHistory,
            newItem: TaskHistory
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }

    // =========================================================================
    // CONSTANTS
    // =========================================================================

    private companion object {

        // ---------------------------------------------------------------------
        // CHANGE TYPES
        // ---------------------------------------------------------------------

        const val CHANGE_TYPE_STATUS_CHANGED =
            "StatusChanged"

        const val CHANGE_TYPE_PRIORITY_CHANGED =
            "PriorityChanged"

        const val CHANGE_TYPE_ASSIGNED_USER_CHANGED =
            "AssignedUserChanged"

        const val CHANGE_TYPE_UPDATED =
            "Updated"

        // ---------------------------------------------------------------------
        // STATUS
        // ---------------------------------------------------------------------

        const val STATUS_TODO =
            "Todo"

        const val STATUS_IN_PROGRESS =
            "InProgress"

        const val STATUS_IN_REVIEW =
            "InReview"

        const val STATUS_DONE =
            "Done"

        // ---------------------------------------------------------------------
        // PRIORITY
        // ---------------------------------------------------------------------

        const val PRIORITY_LOW =
            "Low"

        const val PRIORITY_MEDIUM =
            "Medium"

        const val PRIORITY_HIGH =
            "High"

        const val PRIORITY_CRITICAL =
            "Critical"
    }
}