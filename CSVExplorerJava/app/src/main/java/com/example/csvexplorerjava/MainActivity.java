package com.example.csvexplorerjava;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.csvexplorerjava.adapter.RowAdapter;
import com.example.csvexplorerjava.databinding.ActivityMainBinding;
import com.example.csvexplorerjava.entity.HeadersStore;
import com.example.csvexplorerjava.entity.UiState;
import com.example.csvexplorerjava.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Observer<UiState> {

    private ActivityMainBinding binding;
    private
    MainViewModel vm;

    private ArrayAdapter<String> spinnerAdapter;
    private RowAdapter rowAdapter;

    private final String uploadEndpoint = "https://alperensaracdeneme.com/deneme/upload_csv.php";

    private ActivityResultLauncher<String[]> pickCsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(MainViewModel.class);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        binding.spinnerColumn.setAdapter(spinnerAdapter);

        rowAdapter = new RowAdapter(this, item -> {
            Intent intent = new Intent(this, CSVDetailsActivity.class);
            intent.putExtra(CSVDetailsActivity.EXTRA_JSON, item.getDataJson());
            intent.putStringArrayListExtra(
                    CSVDetailsActivity.EXTRA_HEADERS,
                    new ArrayList<>(HeadersStore.load(this))
            );
            startActivity(intent);
        });
        binding.listView.setAdapter(rowAdapter);

        pickCsv = registerForActivityResult(new ActivityResultContracts.OpenDocument(), (Uri uri) -> {
            if (uri != null) vm.onCsvPicked(uri, getContentResolver());
        });

        // Click listeners (implements View.OnClickListener)
        binding.btnPickCsv.setOnClickListener(this);
        binding.btnSearch.setOnClickListener(this);
        binding.btnClear.setOnClickListener(this);
        binding.btnUpload.setOnClickListener(this);
        binding.btnClearDb.setOnClickListener(this);

        // State observer (implements Observer<UiState>)
        vm.getState().observe(this, this);

        vm.init();
    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();

        if (id == R.id.btnPickCsv) {
            pickCsv.launch(new String[]{"text/*", "text/csv", "application/vnd.ms-excel"});
            return;
        }

        if (id == R.id.btnSearch) {
            String q = binding.etQuery.getText() != null ? binding.etQuery.getText().toString() : "";
            String col = binding.spinnerColumn.getSelectedItem() != null
                    ? binding.spinnerColumn.getSelectedItem().toString()
                    : "ALL_COLUMNS";

            vm.setQuery(q);
            vm.setSelectedColumn(col);
            vm.applyFilter();
            return;
        }

        if (id == R.id.btnClear) {
            binding.etQuery.setText("");
            if (spinnerAdapter.getCount() > 0) binding.spinnerColumn.setSelection(0);

            vm.clearFilter();
            rowAdapter.setHighlight("", "ALL_COLUMNS");
            return;
        }

        if (id == R.id.btnUpload) {
            vm.upload(uploadEndpoint, getContentResolver());
            return;
        }

        if (id == R.id.btnClearDb) {
            vm.clearDb();
        }
    }

    // Observer<UiState>
    @Override
    public void onChanged(UiState s) {
        if (s == null) return;

        boolean uploadEnabled = s.canUpload() && !s.isLoading();
        binding.btnUpload.setEnabled(uploadEnabled);
        binding.btnUpload.setAlpha(uploadEnabled ? 1f : 0.5f);

        binding.btnPickCsv.setEnabled(!s.isLoading());
        binding.btnSearch.setEnabled(!s.isLoading());
        binding.btnClear.setEnabled(!s.isLoading());
        binding.btnClearDb.setEnabled(!s.isLoading());

        binding.tvInfo.setText(s.getInfoText());

        String prevSelected = binding.spinnerColumn.getSelectedItem() != null
                ? binding.spinnerColumn.getSelectedItem().toString()
                : null;

        List<String> items = new ArrayList<>();
        items.add("ALL_COLUMNS");
        items.addAll(s.getHeaders());

        spinnerAdapter.clear();
        spinnerAdapter.addAll(items);
        spinnerAdapter.notifyDataSetChanged();

        if (prevSelected != null && !prevSelected.isBlank()) {
            int idx = items.indexOf(prevSelected);
            if (idx >= 0) binding.spinnerColumn.setSelection(idx);
        }

        rowAdapter.setHeaders(s.getHeaders());
        rowAdapter.submit(s.getRecords());
        rowAdapter.setHighlight(s.getQuery(), s.getSelectedColumn());

        if (s.getErrorMessage() != null) {
            Toast.makeText(this, s.getErrorMessage(), Toast.LENGTH_LONG).show();
        }

        if (s.getDownloadUrl() != null) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s.getDownloadUrl())));
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open browser: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                vm.consumeDownloadUrl();
            }
        }
    }
}
