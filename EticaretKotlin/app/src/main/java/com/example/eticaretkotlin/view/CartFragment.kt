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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.adapters.CartItemsAdapter
import com.example.eticaretkotlin.databinding.FragmentCartBinding
import com.example.eticaretkotlin.model.CheckoutRequest
import com.example.eticaretkotlin.repo.CartVMFactory
import com.example.eticaretkotlin.repo.OrdersVMFactory
import com.example.eticaretkotlin.viewmodel.CartViewModel
import com.example.eticaretkotlin.viewmodel.OrdersViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val ordersVm: OrdersViewModel by viewModels { OrdersVMFactory(requireContext()) }

    private val vm: CartViewModel by viewModels { CartVMFactory(requireContext()) }

    private lateinit var adapter: CartItemsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        binding.btnCheckout.setOnClickListener {
            val req = CheckoutRequest(
                addressName = "Ev",
                addressLine1 = "Test Mah. Test Sok. No:1",
                city = "Istanbul",
                district = "Kadikoy",
                postalCode = "34000"
            )
            ordersVm.checkout(req)
        }

        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        adapter = CartItemsAdapter(
            onPlus = { item -> vm.inc(item.item_id, item.quantity) },
            onMinus = { item -> vm.dec(item.item_id, item.quantity) },
            onDelete = { item -> vm.delete(item.item_id) }
        )

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { vm.loadCart() }

        collectState()
        vm.loadCart()
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ordersVm.state.collect { st ->
                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        ordersVm.clearError()
                    }
                    st.lastOrder?.let {
                        Snackbar.make(binding.root, "Sipariş oluşturuldu #${it.orderId}", Snackbar.LENGTH_LONG).show()
                        // Orders tabına git (BottomNav’da)
                        findNavController().navigate(R.id.toSiparisDetay)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    binding.progress.isVisible = st.loading
                    binding.swipeRefresh.isRefreshing = st.loading && binding.swipeRefresh.isRefreshing

                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        vm.clearError()
                    }

                    val cart = st.cart
                    val items = cart?.items ?: emptyList()

                    binding.empty.isVisible = !st.loading && items.isEmpty()
                    binding.rvCart.isVisible = items.isNotEmpty()

                    adapter.setBusyItemId(st.busyItemId)
                    adapter.submitList(items)

                    binding.tvTotalItems.text = "Ürün: ${cart?.total_items ?: 0}"
                    binding.tvTotal.text = "Toplam: ₺${"%.2f".format(cart?.total ?: 0.0)}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
