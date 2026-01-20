package com.example.eticaretkotlin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.adapters.CategoryAdapter
import com.example.eticaretkotlin.adapters.ProductsAdapter
import com.example.eticaretkotlin.databinding.FragmentAnasayfaBinding
import com.example.eticaretkotlin.repo.HomeVMFactory
import com.example.eticaretkotlin.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnasayfaFragment : Fragment() {

    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels { HomeVMFactory() }

    private lateinit var catAdapter: CategoryAdapter
    private lateinit var prodAdapter: ProductsAdapter

    private var isLoadingNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)

        // ✅ Sepete git: BottomNav tab destination
        binding.btnCart.setOnClickListener {
            findNavController().navigate(R.id.cartFragment)
        }

        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        setupUi()
        setupCollectors()

        vm.loadCategories()
        vm.loadProducts(page = 1)
    }

    private fun setupUi() = with(binding) {
        // Sort seçenekleri
        val sorts = listOf("newest", "price_asc", "price_desc")
        spSort.setSimpleItems(sorts.toTypedArray())
        spSort.setOnItemClickListener { _, _, pos, _ ->
            val cur = vm.state.value.filters
            vm.setFilters(cur.copy(sort = sorts[pos]))
        }

        // Kategoriler
        catAdapter = CategoryAdapter { selected ->
            val cur = vm.state.value.filters
            vm.setFilters(cur.copy(cat = selected?.id))
        }
        rvCats.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = catAdapter
        }

        // Ürünler
        prodAdapter = ProductsAdapter { item ->
            findNavController().navigate(
                R.id.toProductDetail,
                Bundle().apply { putInt("productId", item.id) }
            )
        }
        rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = prodAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val lm = recyclerView.layoutManager as GridLayoutManager
                    val total = lm.itemCount
                    val last = lm.findLastVisibleItemPosition()
                    val st = vm.state.value

                    val reachedEnd = last >= total - 4
                    val hasMore = st.items.size < st.total
                    if (!isLoadingNext && hasMore && reachedEnd) {
                        isLoadingNext = true
                        vm.loadProducts(page = st.page + 1)
                    }
                }
            })
        }

        // Arama
        etSearch.setOnEditorActionListener { tv, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = tv.text?.toString().orEmpty().trim()
                val cur = vm.state.value.filters
                vm.setFilters(cur.copy(q = if (q.isEmpty()) null else q))
                true
            } else false
        }

        // İndirim çipi
        chipDiscount.setOnCheckedChangeListener { _, checked ->
            val cur = vm.state.value.filters
            vm.setFilters(cur.copy(discount = checked))
        }

        // Pull to refresh
        swipe.setOnRefreshListener {
            vm.loadProducts(page = 1)
        }
    }

    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { st ->
                    binding.progress.isVisible = st.loading
                    binding.swipe.isRefreshing = false
                    isLoadingNext = false

                    catAdapter.submitList(st.categories)
                    catAdapter.setSelected(st.filters.cat)

                    prodAdapter.submitList(st.items)

                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
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
