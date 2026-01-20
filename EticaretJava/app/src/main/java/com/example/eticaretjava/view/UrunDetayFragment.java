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


import com.example.eticaretjava.databinding.FragmentUrunDetayBinding;
import com.example.eticaretjava.viewmodel.CartViewModel;
import com.example.eticaretjava.viewmodel.HomeViewModel;


public class UrunDetayFragment extends Fragment {


    private FragmentUrunDetayBinding binding;
    private HomeViewModel homeViewModel;
    private CartViewModel cartViewModel;


    private int productId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        binding = FragmentUrunDetayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        homeViewModel = new ViewModelProvider(requireActivity())
                .get(HomeViewModel.class);


        cartViewModel = new ViewModelProvider(requireActivity())
                .get(CartViewModel.class);


        if (getArguments() != null) {
            productId = getArguments().getInt("productId", -1);
        }


        observeState();
        loadProduct();


        binding.btnAddToCart.setOnClickListener(v -> {
            cartViewModel.addItem(productId);
        });
    }


    private void observeState() {
        homeViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.items == null) return;


            state.items.stream()
                    .filter(p -> p.id == productId)
                    .findFirst()
                    .ifPresent(product -> {
                        binding.tvName.setText(product.name);
                        binding.tvPrice.setText(product.price + " ₺");
                        binding.tvDesc.setText(product.stockQty);
                    });
        });


        cartViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }


            if ("add_success".equals(state.lastAction)) {
                Toast.makeText(getContext(), "Sepete eklendi", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadProduct() {
// Liste zaten yüklüyse detay oradan çekiliyor
// Ayrı detay endpoint'in varsa burada çağırabilirsin
    }
}