package com.alperensarac.projectmanagementkotlin.feature.theme

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.theme.AppThemeMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Aydınlık / Karanlık tema seçim dialog'u.
 */
@AndroidEntryPoint
class ThemeModeDialogFragment :
    DialogFragment() {

    private val viewModel:
            ThemeViewModel
            by activityViewModels()

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        val currentMode =
            viewModel
                .themeMode
                .value

        val checkedIndex =
            when (currentMode) {

                AppThemeMode.LIGHT ->
                    INDEX_LIGHT

                AppThemeMode.DARK ->
                    INDEX_DARK
            }

        return MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                R.string.theme_dialog_title
            )
            .setSingleChoiceItems(
                arrayOf(
                    getString(
                        R.string.theme_light
                    ),
                    getString(
                        R.string.theme_dark
                    )
                ),
                checkedIndex
            ) { dialog, selectedIndex ->

                when (selectedIndex) {

                    INDEX_LIGHT ->
                        viewModel
                            .selectLightTheme()

                    INDEX_DARK ->
                        viewModel
                            .selectDarkTheme()
                }

                dialog.dismiss()
            }
            .setNegativeButton(
                android.R.string.cancel,
                null
            )
            .create()
    }

    companion object {

        const val TAG =
            "ThemeModeDialog"

        private const val INDEX_LIGHT =
            0

        private const val INDEX_DARK =
            1
    }
}