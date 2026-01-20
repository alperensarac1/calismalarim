package com.example.eticaretkotlin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eticaretkotlin.databinding.FragmentSiparisDetayBinding
import com.example.eticaretkotlin.adapters.OrderItemsAdapter
import com.example.eticaretkotlin.adapters.OrderLineUi
import com.example.eticaretkotlin.repo.OrdersVMFactory
import com.example.eticaretkotlin.repo.ProductDetailVMFactory
import com.example.eticaretkotlin.viewmodel.OrdersViewModel
import com.example.eticaretkotlin.viewmodel.ProductDetailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SiparisDetayFragment : Fragment() {

    private var _binding: FragmentSiparisDetayBinding? = null
    private val binding get() = _binding!!

    private val ordersVm: OrdersViewModel by viewModels { OrdersVMFactory(requireContext()) }
    private val productVm: ProductDetailViewModel by viewModels { ProductDetailVMFactory(requireContext()) }

    private val orderId: Int by lazy { requireArguments().getInt("orderId", -1) }
    private val productId: Int by lazy { requireArguments().getInt("productId", -1) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSiparisDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        // Mod seç
        when {
            orderId != -1 -> {
                binding.tvOrderHeader.text = "Sipariş #$orderId"
                collectOrderState()
                ordersVm.loadOrderDetail(orderId)
            }
            productId != -1 -> {
                binding.tvOrderHeader.text = "Ürün #$productId"
                collectProductState()
                productVm.load(productId)
            }
            else -> {
                Snackbar.make(binding.root, "Geçersiz detay id", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun collectOrderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ordersVm.state.collectLatest { st ->
                    binding.progress.isVisible = st.loading
                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        ordersVm.clearError()
                    }
                    st.orderDetail?.let { detail ->
                        // burada sipariş detay UI'ını doldur
                        binding.tvTotal.text = "Toplam: ${detail.currency} ${"%.2f".format(detail.totalAmount)}"
                        // items vs adapter...
                    }
                }
            }
        }
    }

    private fun collectProductState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productVm.state.collectLatest { st ->
                    binding.progress.isVisible = st.loading
                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        productVm.clearFlags()
                    }
                    st.product?.let { p ->
                        // aynı layout'u ürün detay için kullanacaksan burada doldur
                        binding.tvTotal.text = "Fiyat: ₺${"%.2f".format(p.price)}"
                        // sepete ekle butonu varsa vs.
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
