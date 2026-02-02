package com.example.kargopaylasimjava.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.di.AppContainer;
import com.example.kargopaylasimjava.dto.AddressDtos;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.viewmodel.AddressViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class EditAddressActivity extends ComponentActivity {

    private AddressViewModel vm;

    private TextInputEditText etTitle, etCity, etDistrict, etNeighborhood, etLine, etPostal;
    private Button btnSave;
    private ProgressBar progress;

    private int addressId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        addressId = getIntent().getIntExtra("address_id", 0);

        AppContainer container = new AppContainer(getApplicationContext());
        vm = new ViewModelProvider(this, new VMFactories.AddressVmFactory(container.repo))
                .get(AddressViewModel.class);

        etTitle = findViewById(R.id.etTitle);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etNeighborhood = findViewById(R.id.etNeighborhood);
        etLine = findViewById(R.id.etLine);
        etPostal = findViewById(R.id.etPostal);

        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        btnSave.setOnClickListener(v -> save());

        observeVm();

        if (addressId > 0) {
            vm.loadById(addressId);
        }
    }

    private void observeVm() {
        vm.defaultState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);
                AddressDtos.AddressDto a = (AddressDtos.AddressDto) ((UiState.Success<?>) st).data;
                if (a != null) fill(a);
            } else if (st instanceof UiState.Error) {
                setLoading(false);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                setLoading(false);
            }
        });

        vm.saveState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);
                Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (st instanceof UiState.Error) {
                setLoading(false);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                setLoading(false);
            }
        });
    }

    private void fill(AddressDtos.AddressDto a) {
        etTitle.setText(nullToEmpty(a.title));
        etCity.setText(nullToEmpty(a.city));
        etDistrict.setText(nullToEmpty(a.district));
        etNeighborhood.setText(nullToEmpty(a.neighborhood));
        etLine.setText(nullToEmpty(a.address_line));
        etPostal.setText(nullToEmpty(a.postal_code));
    }

    private void save() {
        String title = text(etTitle);
        String city = text(etCity);
        String district = text(etDistrict);
        String neighborhood = text(etNeighborhood);
        String line = text(etLine);
        String postal = text(etPostal);

        if (title.isEmpty() || city.isEmpty() || district.isEmpty() || line.isEmpty()) {
            Toast.makeText(this, "Başlık/şehir/ilçe/açık adres zorunlu.", Toast.LENGTH_LONG).show();
            return;
        }

        vm.saveOrCreate(
                addressId,
                title,
                city,
                district,
                neighborhood,
                line,
                postal
        );
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
