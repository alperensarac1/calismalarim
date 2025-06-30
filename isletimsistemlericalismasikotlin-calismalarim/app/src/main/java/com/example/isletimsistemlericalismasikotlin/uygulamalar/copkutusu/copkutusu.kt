package com.example.isletimsistemlericalismasikotlin.uygulamalar.copkutusu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.anaekran.entity.Const
import com.example.isletimsistemlericalismasikotlin.anaekran.model.UygulamalarModel
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentCopkutusuBinding


class copkutusu : Fragment() {

    private lateinit var binding: FragmentCopkutusuBinding
    private var copkutusuUygulamaListesi: ArrayList<UygulamalarModel> = arrayListOf()
    private lateinit var adapter: CopKutusuAdapter
    private var gelenconsts: Const? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCopkutusuBinding.inflate(inflater, container, false)

        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.copkutusuRV.setHasFixedSize(true)
        binding.copkutusuRV.layoutManager = layoutManager
        binding.copkutusuRV.adapter = adapter

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gelenconsts = arguments?.getSerializable("copler") as? Const
        copkutusuUygulamaListesi = gelenconsts?.getCopUygulamaListesi() ?: arrayListOf()

        adapter = CopKutusuAdapter(requireActivity(), gelenconsts!!)
    }
}
