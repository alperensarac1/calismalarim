package com.example.yardimuygulamakotlin.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.adapter.OpenHelpAdapter
import com.example.yardimuygulamakotlin.entity.Session
import com.example.yardimuygulamakotlin.repo.HelperRepo
import com.example.yardimuygulamakotlin.service.Poller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelperOpenListFragment : Fragment(R.layout.fragment_helper_open_list) {

    private var helperId: Long = 0L
    private val repo = HelperRepo()
    private lateinit var btnAccepted: Button
    private lateinit var btnLogout: Button
    private lateinit var rv: RecyclerView
    private lateinit var tvSub: TextView
    private lateinit var progress: ProgressBar
    private lateinit var adapter: OpenHelpAdapter
    private lateinit var btnHistory: Button

    private var poller: Poller? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helperId = requireArguments().getLong("helper_id")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvOpen)
        tvSub = view.findViewById(R.id.tvSub)
        progress = view.findViewById(R.id.progress)

        adapter = OpenHelpAdapter(emptyList()) { item ->
            acceptRequest(item.id)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        poller = Poller(viewLifecycleOwner.lifecycleScope, intervalMs = 4000) {
            fetchOpen()
        }
        btnAccepted = view.findViewById(R.id.btnAccepted)
        btnLogout = view.findViewById(R.id.btnLogout)

        btnAccepted.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, HelperAcceptedFragment.newInstance(helperId))
                .addToBackStack(null)
                .commit()
        }
        btnHistory = view.findViewById(R.id.btnHistory)

        btnHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, HelperConfirmedFragment.newInstance(helperId))
                .addToBackStack(null)
                .commit()
        }
        btnLogout.setOnClickListener {
            Session.clear(requireContext())
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        poller?.start()
    }

    override fun onStop() {
        super.onStop()
        poller?.stop()
    }

    private suspend fun fetchOpen() {
        val res = repo.listOpen(helperId)
        withContext(Dispatchers.Main) {
            if (res?.ok == true) {
                val items = res.items ?: emptyList()
                adapter.submit(items)
                tvSub.text = "Bulunan: ${items.size} (ilçe: ${res.helper_ilce ?: "-"})"
            } else {
                tvSub.text = "Liste alınamadı"
            }
        }
    }

    private fun acceptRequest(requestId: Long) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.accept(requestId, helperId)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                if (res?.ok == true) {
                    // kabul ekranına geç
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, HelperAcceptedFragment.newInstance(helperId))
                        .addToBackStack(null)
                        .commit()
                } else {
                    tvSub.text = "İstek alınamadı (başkası kabul etmiş olabilir)"
                }
            }
        }
    }

    companion object {
        fun newInstance(helperId: Long): HelperOpenListFragment {
            val f = HelperOpenListFragment()
            f.arguments = Bundle().apply { putLong("helper_id", helperId) }
            return f
        }
    }
}