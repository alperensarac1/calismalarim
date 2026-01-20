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
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.eticaretjava.adapter.CartAdapter;
import com.example.eticaretjava.databinding.FragmentCartBinding;
import com.example.eticaretjava.viewmodel.CartViewModel;


public class CartFragment extends Fragment {


    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewModel = new ViewModelProvider(requireActivity())
                .get(CartViewModel.class);


        adapter = new CartAdapter(
                item -> viewModel.updateQuantity(item.item_id, item.quantity + 1),
                item -> {
                    int next = item.quantity - 1;
                    if (next <= 0) viewModel.removeItem(item.item_id);
                    else viewModel.updateQuantity(item.item_id, next);
                },
                item -> viewModel.removeItem(item.item_id)
        );

        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);

        viewModel.getState().observe(getViewLifecycleOwner(), s -> {
            binding.progress.setVisibility(s.loading ? View.VISIBLE : View.GONE);

            if (s.error != null) {
                Toast.makeText(getContext(), s.error, Toast.LENGTH_SHORT).show();
            }

            if (s.cart != null) {
                adapter.submitList(s.cart.items);
                binding.tvTotal.setText("₺" + String.format("%.2f", s.cart.total));
            }

            adapter.setBusyItemId(s.busyItemId);
        });



        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);


        observeState();


        viewModel.loadCart();
    }


    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {


            binding.progress.setVisibility(
                    state.loading ? View.VISIBLE : View.GONE
            );


            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }


            if (state.cart != null) {
                adapter.submitList(state.cart.items);
                binding.tvTotal.setText(state.cart.total + " ₺");
            }
        });
    }
}