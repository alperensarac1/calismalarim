package com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentRehberBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.model.RehberKisiler

class RehberFragment : Fragment() {

    private lateinit var binding: FragmentRehberBinding
    private lateinit var vt: RehberVeritabaniYardimcisi

    private lateinit var kisilerArrayList: ArrayList<RehberKisiler>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var kisiAdlar: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRehberBinding.inflate(inflater, container, false)

        vt = RehberVeritabaniYardimcisi(requireContext())
        val dao = RehberDao(vt)

        kisiAdlar = ArrayList()
        kisilerArrayList = dao.tumKisiler()

        kisilerArrayList.forEach { kisiAdlar.add(it.kisi_isim) }

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, kisiAdlar)
        binding.lvRehber.adapter = adapter

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 101
            )
        }

        binding.lvRehber.setOnItemClickListener { _, _, position, _ ->
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:+9${kisilerArrayList[position].kisi_numara}")
            startActivity(callIntent)
        }

        binding.lvRehber.setOnItemLongClickListener { view, _, position, _ ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.rehber_duzenle_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.rehber_duzenle_action -> {
                        val kisi = kisilerArrayList[position]
                        val fragmentRehberDetay = RehberDetayFragment()
                        val args = Bundle().apply {
                            putSerializable("kisi", kisi)
                        }
                        fragmentRehberDetay.arguments = args
                        Navigation.findNavController(view).navigate(R.id.toRehberDetayFragment, args)
                        true
                    }

                    R.id.rehber_sil_action -> {
                        dao.kisiSil(kisilerArrayList[position].kisi_id)
                        true
                    }

                    else -> false
                }
            }
            popup.show()
            true
        }

        binding.fabEkle.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.rehberEkleFragment)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}
