package com.example.eticaretjava.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eticaretjava.R;
import com.example.eticaretjava.adapter.ProductsAdapter;
import com.example.eticaretjava.databinding.FragmentAnasayfaBinding;
import com.example.eticaretjava.viewmodel.HomeViewModel;

public class AnasayfaFragment extends Fragment {

    private FragmentAnasayfaBinding binding;
    private HomeViewModel viewModel;
    private ProductsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnasayfaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(HomeViewModel.class);

        // ✅ ProductsAdapter artık click callback alıyor
        adapter = new ProductsAdapter(item -> {
            // ürün detayına git (Navigation varsa)
            Bundle b = new Bundle();
            b.putInt("productId", item.id);

            // NavGraph’ta action id’n farklıysa bunu değiştir
            NavHostFragment.findNavController(this)
                    .navigate(R.id.toProductDetail, b);
        });

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProducts.setAdapter(adapter);

        observeState();

        viewModel.loadCategories();
        viewModel.loadProducts(1);
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {

            binding.progress.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }

            adapter.submitList(state.items);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // ✅ memory leak önlemi
    }
}
