package com.example.memesharekotlinn.view

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memesharekotlinn.R
import com.example.memesharekotlinn.adapter.OdaAdapter
import com.example.memesharekotlinn.databinding.FragmentAnasayfaBinding
import com.example.memesharekotlinn.model.OdaModel
import com.example.memesharekotlinn.model.SimpleResponse
import com.example.memesharekotlinn.service.ApiClient
import com.example.memesharekotlinn.viewmodel.OdaViewModel

class AnasayfaFragment : Fragment() {

    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!

    private var userId: Int = 0
    private lateinit var odaViewModel: OdaViewModel
    private val odaListesi = mutableListOf<OdaModel>()
    private lateinit var adapter: OdaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        odaViewModel = ViewModelProvider(requireActivity())[OdaViewModel::class.java]
        userId = arguments?.getInt("userId", 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)

        // RecyclerView
        adapter = OdaAdapter(odaListesi, object : OdaAdapter.OnOdaClickListener {
            override fun onOdaClick(oda: OdaModel) {
                val bundle = Bundle().apply {
                    putInt("roomId", oda.odaId)
                    putInt("userId", userId)
                }
                findNavController()
                    .navigate(R.id.action_anasayfaFragment_to_fragmentOda, bundle)
                println("Odaya tıklandı ${oda.odaId}")
            }
        })

        binding.rvOdalar.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOdalar.adapter = adapter

        // Odaya katıl (oda kodu gir)
        binding.btnOdaKayit.setOnClickListener {
            val input = EditText(requireContext()).apply {
                hint = "Oda ID girin"
                inputType = InputType.TYPE_CLASS_TEXT
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Oda Katılım")
                .setView(input)
                .setPositiveButton("Katıl") { _, _ ->
                    val odaIdStr = input.text.toString().trim()
                    if (odaIdStr.isNotEmpty()) {
                        ApiClient.getService().joinRoom(userId, odaIdStr)
                            .enqueue(object : retrofit2.Callback<SimpleResponse> {
                                override fun onResponse(
                                    call: retrofit2.Call<SimpleResponse>,
                                    response: retrofit2.Response<SimpleResponse>
                                ) {
                                    val body = response.body()
                                    if (response.isSuccessful && body != null && body.success) {
                                        Toast.makeText(requireContext(), "Odaya katıldınız", Toast.LENGTH_SHORT).show()
                                        fetchOdalar()
                                    } else {
                                        Toast.makeText(requireContext(), "Katılım başarısız", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(
                                    call: retrofit2.Call<SimpleResponse>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(requireContext(), "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
                .setNegativeButton("İptal") { dialog, _ -> dialog.cancel() }
                .show()
        }

        // Odaları getir
        fetchOdalar()

        // Oda oluştur
        binding.btnOdaGiris.setOnClickListener { odaOlustur() }

        return binding.root
    }

    private fun fetchOdalar() {
        ApiClient.getService().getJoinedRooms(userId)
            .enqueue(object : retrofit2.Callback<List<OdaModel>> {
                override fun onResponse(
                    call: retrofit2.Call<List<OdaModel>>,
                    response: retrofit2.Response<List<OdaModel>>
                ) {
                    val list = response.body()
                    if (response.isSuccessful && list != null) {
                        odaListesi.clear()
                        odaListesi.addAll(list)
                        adapter.notifyDataSetChanged()
                        list.forEach { println(it.roomCode) }
                    } else {
                        Toast.makeText(requireContext(), "Odalar getirilemedi", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<OdaModel>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun odaOlustur() {
        odaViewModel.createRoom(userId)

        odaViewModel.odaOlusturmaSonucu.observe(viewLifecycleOwner) { sonuc ->
            if (sonuc.success) {
                Toast.makeText(requireContext(), "Oda oluşturuldu: ${sonuc.roomCode}", Toast.LENGTH_SHORT).show()

                val yeniOda = OdaModel(
                    odaId = sonuc.roomId!!,       // ✅ room_id
                    roomCode = sonuc.roomCode!!,  // ✅ room_code
                    createdBy = userId
                )
                odaListesi.add(yeniOda)
                adapter.notifyItemInserted(odaListesi.size - 1)
            } else {
                Toast.makeText(requireContext(), "Hata: ${sonuc.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
