package com.example.eticaretkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.databinding.FragmentUrunDetayBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.eticaretkotlin.repo.ProductDetailVMFactory
import com.example.eticaretkotlin.viewmodel.ProductDetailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UrunDetayFragment: Fragment() {

    private var _binding: FragmentUrunDetayBinding? = null
    private val binding get() = _binding!!

    private val vm: ProductDetailViewModel by viewModels {
        ProductDetailVMFactory(requireContext())
    }

    private var qty = 1
    private val productId: Int by lazy {
        // nav_graph’ta arg ismi "productId" varsayıldı
        requireArguments().getInt("productId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentUrunDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        // adet butonları
        binding.btnInc.setOnClickListener { qty = (qty + 1).coerceAtMost(99); binding.tvQty.text = qty.toString() }
        binding.btnDec.setOnClickListener { qty = (qty - 1).coerceAtLeast(1); binding.tvQty.text = qty.toString() }

        binding.btnAddToCart.setOnClickListener {
            vm.addToCart(productId, qty)
        }

        // state collect
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { st ->
                    binding.progress.isVisible = st.loading

                    st.product?.let { p ->
                        binding.tvName.text = p.name
                        binding.tvPrice.text = "₺${"%.2f".format(p.price)}"
                        binding.tvDesc.text = p.slug ?: ""
                        Glide.with(binding.img).load(p.imageUrl).into(binding.img)
                    }

                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }

                    if (st.addSuccess) {
                        Snackbar.make(binding.root, "Sepete eklendi", Snackbar.LENGTH_SHORT).show()
                        vm.clearFlags()
                    }
                }
            }
        }

        // ürünü yükle
        vm.load(productId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
