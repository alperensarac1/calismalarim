package com.example.eticaretkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.adapters.SiparislerAdapter
import com.example.eticaretkotlin.databinding.FragmentSiparislerBinding
import com.example.eticaretkotlin.repo.OrdersVMFactory
import com.example.eticaretkotlin.viewmodel.OrdersViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SiparislerFragment : Fragment(R.layout.fragment_siparisler) {

    private var _binding: FragmentSiparislerBinding? = null
    private val binding get() = _binding!!

    private val vm: OrdersViewModel by viewModels { OrdersVMFactory(requireContext()) }
    private lateinit var adapter: SiparislerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSiparislerBinding.bind(view)

        adapter = SiparislerAdapter { orderId ->
            val b = Bundle().apply { putInt("orderId", orderId) }
            findNavController().navigate(R.id.toSiparisDetay, b)
        }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        collectState()
        vm.loadOrders()
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    binding.progress.isVisible = st.loading
                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        vm.clearError()
                    }
                    binding.empty.isVisible = !st.loading && st.orders.isEmpty()
                    adapter.submitList(st.orders)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
