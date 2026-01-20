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

import com.example.eticaretjava.adapter.SiparislerAdapter;
import com.example.eticaretjava.databinding.FragmentSiparislerBinding;
import com.example.eticaretjava.viewmodel.OrdersViewModel;


public class SiparislerFragment extends Fragment {


    private FragmentSiparislerBinding binding;
    private OrdersViewModel viewModel;
    private SiparislerAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSiparislerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewModel = new ViewModelProvider(requireActivity())
                .get(OrdersViewModel.class);


        adapter = new SiparislerAdapter(id -> {
            viewModel.loadOrderDetail(id);
        });


        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(adapter);


        observeState();
        viewModel.loadOrders();
    }


    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {


            binding.progress.setVisibility(
                    state.loading ? View.VISIBLE : View.GONE
            );


            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }


            adapter.submitList(state.orders);
        });
    }
}
