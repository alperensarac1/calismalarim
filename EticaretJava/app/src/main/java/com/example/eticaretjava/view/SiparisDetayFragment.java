package com.example.eticaretjava.view;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.eticaretjava.databinding.FragmentSiparisDetayBinding;
import com.example.eticaretjava.viewmodel.OrdersViewModel;


public class SiparisDetayFragment extends Fragment {


    private FragmentSiparisDetayBinding binding;
    private OrdersViewModel viewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSiparisDetayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewModel = new ViewModelProvider(requireActivity())
                .get(OrdersViewModel.class);


        observeState();
    }


    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.orderDetail != null) {
                binding.tvOrderHeader.setText(String.valueOf(state.orderDetail.id));
                binding.tvTotal.setText(state.orderDetail.totalAmount + " ₺");
            }
        });
    }
}
