package com.example.yardimuygulamakotlin.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.adapter.ConfirmedHelpAdapter
import com.example.yardimuygulamakotlin.repo.HelperRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelperConfirmedFragment : Fragment(R.layout.fragment_helper_confirmed) {

    private var helperId: Long = 0L
    private val repo = HelperRepo()

    private lateinit var rv: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: ConfirmedHelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helperId = requireArguments().getLong("helper_id")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvConfirmed)
        tvInfo = view.findViewById(R.id.tvInfo)

        adapter = ConfirmedHelpAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        load()
    }

    private fun load() {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.myConfirmed(helperId)
            withContext(Dispatchers.Main) {
                if (res?.ok == true) {
                    val items = res.items ?: emptyList()
                    adapter.submit(items)
                    tvInfo.text = "Toplam: ${items.size}"
                } else {
                    tvInfo.text = "Geçmiş alınamadı"
                }
            }
        }
    }

    companion object {
        fun newInstance(helperId: Long): HelperConfirmedFragment {
            val f = HelperConfirmedFragment()
            f.arguments = Bundle().apply { putLong("helper_id", helperId) }
            return f
        }
    }
}