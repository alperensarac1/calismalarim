package com.example.adisyonuygulamakotlin.view.anaekran

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentManager
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.utils.paddingEkle
import com.example.adisyonuygulamakotlin.view.masadetay.MasaDetayFragment
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

class MainLayout(
    val context: Context,
    var masaListesi: List<Masa>,
    val fragmentManager: FragmentManager,
    val viewModel: MasaDetayViewModel,
    val onMasaClick: (Masa) -> Unit
) {

    fun linearLayout(): LinearLayout {
        return LinearLayout(context).apply {
            setBackgroundColor(Color.parseColor("#FFBABA"))
            orientation = LinearLayout.HORIZONTAL
            paddingEkle(10, 10, 10, 10)

            val masaOzet = MasaOzetLayout(
                context = context,
                masaListesi = masaListesi,
                viewModel = viewModel,
                onMasaDetayTikla = { masa ->
                    fragmentManager.beginTransaction()
                        .replace(
                            (this@apply.parent as? ViewGroup)?.id ?: View.generateViewId(), // fallback
                            MasaDetayFragment.newInstance(masa.id)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            ).linearLayout().apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
            }

            val masalar = MasalarLayout(context, masaListesi, onMasaClick).recyclerView().apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 2f
                )
            }

            addView(masaOzet)
            addView(masalar)
        }
    }
}

