package com.example.adisyonuygulamakotlin.view.masadetay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel
import com.example.adisyonuygulamakotlin.viewmodel.MasalarViewModel
import com.example.adisyonuygulamakotlin.viewmodel.UrunViewModel

class MasaDetayFragment : Fragment() {

    private val viewModel: MasaDetayViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val masaId = arguments?.getInt("masa_id") ?: -1
                return MasaDetayViewModel(masaId) as T
            }
        }
    }

    private val urunViewModel: UrunViewModel by viewModels()

    private lateinit var layout: MasaDetayLayout
    private lateinit var containerLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        containerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        urunViewModel.kategorileriYukle()
        return containerLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val masaId = arguments?.getInt("masa_id") ?: -1
        val masa = Masa(masaId, "", 1, "00:00", 0f)

        layout = MasaDetayLayout(
            context = requireContext(),
            masa = masa,
            viewModel = viewModel,
            urunVM = urunViewModel
        )

        containerLayout.addView(layout.show())

        // 1. Ürün ve kategori yüklemesi
        urunViewModel.urunleriYukle()

        urunViewModel.urunler.observe(viewLifecycleOwner) { urunler ->
            layout.urunlerLayout.setUrunListesi(urunler)

        }
        urunViewModel.kategoriler.observe(viewLifecycleOwner){kategoriler->
            layout.urunlerLayout.setKategoriListesi(kategoriler)
        }
        // 2. Toplam fiyatı güncelle
        viewModel.toplamFiyat.observe(viewLifecycleOwner) { fiyat ->
            layout.masaAdisyonLayout.guncelleToplamFiyat(fiyat)
        }

        // 3. Ödeme tamamlandıysa ana sayfaya geri dön ve yenile
        viewModel.odemeTamamlandi.observe(viewLifecycleOwner) { tamamlandi ->
            if (tamamlandi) {
                Toast.makeText(requireContext(), "Ödeme tamamlandı Fragment", Toast.LENGTH_SHORT).show()
                viewModel.yukleTumVeriler()
                requireActivity().onBackPressed()
                // masa nesnesi güncellendiyse:
                viewModel.odemeAl {
                    val guncelMasa = viewModel.masa.value ?: return@odemeAl

                    val activityViewModel = ViewModelProvider(requireActivity())[MasalarViewModel::class.java]
                    activityViewModel.guncelleMasa(guncelMasa)

                    parentFragmentManager.popBackStack()
                }

            }
        }

        // 4. Masaya ait ürünleri yükle
        viewModel.yukleTumVeriler()
    }


    companion object {
        fun newInstance(masaId: Int) = MasaDetayFragment().apply {
            arguments = Bundle().apply {
                putInt("masa_id", masaId)
            }
        }
    }
}
