package com.example.adisyonuygulamakotlin.view.anaekran

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.view.masadetay.MasaDetayFragment
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel
import com.example.adisyonuygulamakotlin.viewmodel.MasalarViewModel
import com.example.adisyonuygulamakotlin.viewmodel.UrunViewModel

class MainFragment : Fragment() {
    private val RESIM_SEC_KODU = 1001
    private var secilenResimBase64: String? = null
    private lateinit var masaList: List<Masa>
    private lateinit var containerLayout: LinearLayout  // View'ları dinamik değiştirebilmek için
    private lateinit var layout: MainLayout
    private val urunVM: UrunViewModel by viewModels()
    private val viewModel: MasalarViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        masaList = arguments?.getSerializable("masalar") as? List<Masa> ?: emptyList()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        containerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Gözlemlenecek veriler yüklensin
        viewModel.masalariYukle()
        urunVM.kategorileriYukle()

        viewModel.masalar.observe(viewLifecycleOwner) { masaListesi ->
            containerLayout.removeAllViews()
            val layout = MainLayout(
                context = requireContext(),
                masaListesi = masaListesi, fragmentManager = parentFragmentManager,
                viewModel = MasaDetayViewModel(0),
                onMasaClick = { masa ->
                    parentFragmentManager.beginTransaction()
                        .replace(
                            (container?.id ?: View.generateViewId()),
                            MasaDetayFragment.newInstance(masa.id)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            )
            containerLayout.addView(layout.linearLayout())
            ekIslemButonlariniEkle(containerLayout)
        }

        return containerLayout
    }

    companion object {
        fun newInstance(masalar: List<Masa>) = MainFragment().apply {
            arguments = Bundle().apply {
                putSerializable("masalar", ArrayList(masalar))
            }
        }
    }

    private fun masaEkleCikarDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Silinecek masa ID"
            inputType = InputType.TYPE_CLASS_NUMBER
            setPadding(50, 40, 50, 20)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Masa İşlemleri")
            .setMessage("Yeni masa ekleyebilir veya masa ID girerek masa silebilirsiniz.")
            .setView(input)
            .setPositiveButton("Masa Ekle") { dialog, _ ->
                viewModel.masaEkle {
                    Toast.makeText(requireContext(), "Masa eklendi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    viewModel.masalariYukle()
                }
            }
            .setNeutralButton("Masa Sil") { dialog, _ ->
                val masaIdText = input.text.toString()
                if (masaIdText.isNotBlank()) {
                    val masaId = masaIdText.toIntOrNull()
                    if (masaId != null) {
                        viewModel.masaSil(masaId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Geçerli bir ID giriniz",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Masa ID giriniz", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun ekIslemButonlariniEkle(container: LinearLayout) {
        // 1) Butonları yatay tutacak layout
        val butonLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 2) Helper: weight li LayoutParams üretici
        fun wParams(start: Int = 0, end: Int = 0) =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = start
                marginEnd = end
            }

        // 3) Masa İşlemleri
        val btnMasa = Button(requireContext()).apply {
            text = "Masa İşlemleri"
            layoutParams = wParams(end = 10)
            setOnClickListener { masaEkleCikarDialog() }
        }

        // 4) Ürün İşlemleri
        val btnUrun = Button(requireContext()).apply {
            text = "Ürün İşlemleri"
            layoutParams = wParams(start = 10, end = 10)
            setOnClickListener {
                urunVM.kategoriler.value?.let { urunEkleVeSilDialog(it) }
            }
        }

        // 5) Masa Birleştir
        val btnBirlestir = Button(requireContext()).apply {
            text = "Masa Birleştir"
            layoutParams = wParams(start = 10, end = 10)
            setOnClickListener { masaBirlestirDialog() }
        }

        // 6) Kategori İşlemleri
        val btnKategori = Button(requireContext()).apply {
            text = "Kategori İşlemleri"
            layoutParams = wParams(start = 10)
            setOnClickListener {
                val kategoriler = urunVM.kategoriler.value
                kategoriEkleVeSilDialog(kategoriler!!)
                urunVM.kategorileriYukle()
            }

        }

        // 7) Butonları sırasıyla ekle
        butonLayout.addView(btnMasa)
        butonLayout.addView(btnUrun)
        butonLayout.addView(btnBirlestir)
        butonLayout.addView(btnKategori)

        // 8) Container'ın sonuna ekle
        container.addView(butonLayout)
    }


    private fun kategoriEkleVeSilDialog(kategoriler: List<Kategori>) {
        // 1) Layout
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }

        // 2) Ekleme için EditText
        val kategoriAdEt = EditText(requireContext()).apply {
            hint = "Yeni Kategori Adı"
        }
        layout.addView(kategoriAdEt)

        // 3) Silme için Spinner
        val kategoriAdlari = kategoriler.map { it.kategori_ad }
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                kategoriAdlari
            )
        }
        layout.addView(spinner)

        // 4) Dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Kategori İşlemleri")
            .setView(layout)
            // Kategori ekle
            .setPositiveButton("Kaydet") { dialog, _ ->
                val ad = kategoriAdEt.text.toString().trim()
                if (ad.isNotEmpty()) {
                    urunVM.kategoriEkle(ad)
                    urunVM.kategorileriYukle()
                } else {
                    Toast.makeText(requireContext(), "Ad girin!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            // Kategori sil
            .setNeutralButton("Sil") { dialog, _ ->
                val pos = spinner.selectedItemPosition
                val kategoriId = kategoriler[pos].id
                urunVM.kategoriSil(kategoriId)
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }


    // Dialog metodunu önceki örnekteki gibi ekleyin:
    private fun masaBirlestirDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val anaIdEt = EditText(requireContext()).apply {
            hint = "Ana Masa ID"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val bIdEt = EditText(requireContext()).apply {
            hint = "Birleştirilecek Masa ID"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(anaIdEt)
        layout.addView(bIdEt)

        AlertDialog.Builder(requireContext())
            .setTitle("Masa Birleştir")
            .setView(layout)
            .setPositiveButton("Birleştir") { dialog, _ ->
                val anaId = anaIdEt.text.toString().toIntOrNull()
                val bId = bIdEt.text.toString().toIntOrNull()
                if (anaId == null || bId == null) {
                    Toast.makeText(
                        requireContext(),
                        "Lütfen geçerli iki ID girin",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // ViewModel çağrısı
                    viewModel.masaBirlestir(anaId, bId)
                    viewModel.masalariYukle()
                }
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC_KODU && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            secilenResimBase64 = uriToBase64(uri)

            // İsteğe bağlı: seçilen resmi göster
            Toast.makeText(requireContext(), "Resim seçildi", Toast.LENGTH_SHORT).show()
        }
    }

    private val resimSecLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                secilenResimBase64 = uriToBase64(it)
                Toast.makeText(requireContext(), "Resim seçildi", Toast.LENGTH_SHORT).show()
            }
        }


    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun urunEkleVeSilDialog(kategoriler: List<Kategori>) {
        // 1) Root layout
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        // —–––––– ÜRÜN EKLEME BÖLÜMÜ ––––––—
        val urunAdEt = EditText(requireContext()).apply { hint = "Ürün Adı" }
        val fiyatEt = EditText(requireContext()).apply {
            hint = "Ürün Fiyatı"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val kategoriSpinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                kategoriler.map { it.kategori_ad }
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        val resimButton = Button(requireContext()).apply {
            text = "Resim Seç"
            setOnClickListener { resimSecLauncher.launch("image/*") }
        }

        layout.apply {
            addView(TextView(requireContext()).apply { text = "Ürün Ekle"; textSize = 16f })
            addView(urunAdEt)
            addView(fiyatEt)
            addView(kategoriSpinner)
            addView(resimButton)
            addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 2)
                    .apply { topMargin = 20; bottomMargin = 20 }
                setBackgroundColor(Color.LTGRAY)
            })
        }

// —–––––– ÜRÜN SİLME BÖLÜMÜ ––––––—
        layout.addView(TextView(requireContext()).apply {
            text = "Ürün Sil"
            textSize = 16f
        })

        // Sadece ad girmek için EditText
        val silAdEditText = EditText(requireContext()).apply {
            hint = "Silinecek ürünün adı"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        layout.addView(silAdEditText)

        // 3) Dialog’u oluştur
        AlertDialog.Builder(requireContext())
            .setView(layout)
            .setPositiveButton("Ekle") { dialog, _ ->
                // … [Ürün ekleme aynen] …
            }
            .setNeutralButton("Sil") { dialog, _ ->
                val ad = silAdEditText.text.toString().trim()
                if (ad.isNotEmpty()) {
                    urunVM.urunSil(ad)
                    urunVM.urunleriYukle()
                    Toast.makeText(
                        requireContext(),
                        "\"$ad\" silme isteği gönderildi",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Lütfen silinecek ürün adını girin!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}



