package com.example.kargopaylasimjava.view;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.adapter.AddressAdapter;
import com.example.kargopaylasimjava.adapter.ShipmentAdapter;
import com.example.kargopaylasimjava.di.AppContainer;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.viewmodel.AddressListViewModel;
import com.example.kargopaylasimjava.viewmodel.ShipmentViewModel;

import java.util.List;

public class HomeActivity extends ComponentActivity {

    private ShipmentViewModel vm;
    private ShipmentAdapter adapter;

    private AddressListViewModel addrVm;
    private AddressAdapter addrAdapter;

    private final ActivityResultLauncher<Intent> createShipmentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK) vm.loadShipments();
            });

    private final ActivityResultLauncher<Intent> editAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK) addrVm.load();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppContainer container = new AppContainer(getApplicationContext());
        if (!container.tokenStore.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        vm = new ViewModelProvider(this, new VMFactories.ShipmentVmFactory(container.repo))
                .get(ShipmentViewModel.class);

        addrVm = new ViewModelProvider(this, new VMFactories.AddressListVmFactory(container.repo))
                .get(AddressListViewModel.class);

        Button btnNew = findViewById(R.id.btnNew);
        Button btnAddress = findViewById(R.id.btnAddress);

        RecyclerView rvShip = findViewById(R.id.rvShipments);
        ProgressBar prog = findViewById(R.id.progress);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rvAddr = findViewById(R.id.rvAddresses);
        TextView tvAddrEmpty = findViewById(R.id.tvAddrEmpty);

        SwipeRefreshLayout swShip = findViewById(R.id.swShipments);

        adapter = new ShipmentAdapter(null, item -> {
            Intent i = new Intent(this, ShipmentDetailActivity.class);
            i.putExtra("shipment_id", item.id);
            startActivity(i);
        });
        rvShip.setLayoutManager(new LinearLayoutManager(this));
        rvShip.setAdapter(adapter);

        addrAdapter = new AddressAdapter(
                a -> {
                    Intent i = new Intent(this, EditAddressActivity.class);
                    i.putExtra("address_id", a.id);
                    editAddressLauncher.launch(i);
                },
                a -> addrVm.setDefault(a.id),
                a -> addrVm.delete(a.id)
        );
        rvAddr.setLayoutManager(new LinearLayoutManager(this));
        rvAddr.setAdapter(addrAdapter);

        btnNew.setOnClickListener(v -> createShipmentLauncher.launch(new Intent(this, CreateShipmentActivity.class)));

        swShip.setOnRefreshListener(vm::loadShipments);

        btnAddress.setOnClickListener(v -> editAddressLauncher.launch(new Intent(this, EditAddressActivity.class)));

        vm.listState.observe(this, st -> {
            if (swShip.isRefreshing()) swShip.setRefreshing(false);

            if (st instanceof UiState.Loading) {
                prog.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);

            } else if (st instanceof UiState.Success) {
                prog.setVisibility(View.GONE);

                @SuppressWarnings("unchecked")
                List<com.example.kargopaylasimjava.dto.ShipmentDtos.ShipmentDto> list =
                        (List<com.example.kargopaylasimjava.dto.ShipmentDtos.ShipmentDto>)
                                ((UiState.Success<?>) st).data;

                if (list == null) list = java.util.Collections.emptyList();

                adapter.submit(list);
                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

            } else if (st instanceof UiState.Error) {
                prog.setVisibility(View.GONE);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            }
        });


        addrVm.listState.observe(this, st -> {
            if (st instanceof UiState.Success) {

                @SuppressWarnings("unchecked")
                List<com.example.kargopaylasimjava.dto.AddressDtos.AddressDto> list =
                        (List<com.example.kargopaylasimjava.dto.AddressDtos.AddressDto>)
                                ((UiState.Success<?>) st).data;

                if (list == null) list = java.util.Collections.emptyList();

                addrAdapter.submit(list);
                tvAddrEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

            } else if (st instanceof UiState.Error) {
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            }
        });


        addrVm.setDefaultState.observe(this, st -> {
            if (st instanceof UiState.Success) addrVm.load();
            else if (st instanceof UiState.Error) Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
        });

        addrVm.deleteState.observe(this, st -> {
            if (st instanceof UiState.Success) addrVm.load();
            else if (st instanceof UiState.Error) Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
        });

        vm.loadShipments();
        addrVm.load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.loadShipments();
        addrVm.load();
    }
}
