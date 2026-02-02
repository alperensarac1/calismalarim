package com.example.kargopaylasimjava.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.di.AppContainer;
import com.example.kargopaylasimjava.dto.ReceiverDtos;
import com.example.kargopaylasimjava.dto.ShipmentDtos;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.util.PhoneUtil;
import com.example.kargopaylasimjava.util.DateUtil;
import com.example.kargopaylasimjava.viewmodel.ShipmentViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class CreateShipmentActivity extends ComponentActivity {

    private ShipmentViewModel vm;

    private TextInputEditText etReceiverPhone;
    private Button btnLookup, btnConfirm, btnCancel, btnCopyCode;
    private TextView tvLookupResult, tvCode, tvExpires;
    private View confirmRow, resultBox;
    private ProgressBar progress;

    private String lastLookupPhoneE164 = null;
    private boolean lookupOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shipment);

        AppContainer container = new AppContainer(getApplicationContext());
        vm = new ViewModelProvider(this, new VMFactories.ShipmentVmFactory(container.repo))
                .get(ShipmentViewModel.class);

        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        btnLookup = findViewById(R.id.btnLookup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        tvLookupResult = findViewById(R.id.tvLookupResult);
        confirmRow = findViewById(R.id.confirmRow);
        progress = findViewById(R.id.progress);

        resultBox = findViewById(R.id.resultBox);
        tvCode = findViewById(R.id.tvCode);
        tvExpires = findViewById(R.id.tvExpires);
        btnCopyCode = findViewById(R.id.btnCopyCode);

        btnLookup.setOnClickListener(v -> doLookup());
        btnConfirm.setOnClickListener(v -> doCreate());
        btnCancel.setOnClickListener(v -> resetLookupUi());
        btnCopyCode.setOnClickListener(v -> copyCodeToClipboard());

        observeVm();
    }

    private void observeVm() {
        vm.lookupState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);

                ReceiverDtos.ReceiverLookupResp data = (ReceiverDtos.ReceiverLookupResp) ((UiState.Success<?>) st).data;
                String name = (data.masked_first_name != null ? data.masked_first_name : "") +
                        " " +
                        (data.masked_last_name != null ? data.masked_last_name : "");
                tvLookupResult.setText("Bulunan: " + name.trim());
                tvLookupResult.setVisibility(View.VISIBLE);

                confirmRow.setVisibility(View.VISIBLE);
                lookupOk = true;

            } else if (st instanceof UiState.Error) {
                setLoading(false);
                lookupOk = false;
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
                tvLookupResult.setVisibility(View.GONE);
                confirmRow.setVisibility(View.GONE);
            } else {
                setLoading(false);
            }
        });

        vm.createState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);

                ShipmentDtos.ShipmentCreateResp data = (ShipmentDtos.ShipmentCreateResp) ((UiState.Success<?>) st).data;

                resultBox.setVisibility(View.VISIBLE);
                tvCode.setText("Kod: " + data.pickup_code);
                tvExpires.setText("Son geçerlilik: " + (data.code_expires_at != null ? data.code_expires_at : "-"));

                // artık ana ekran yenilesin diye OK dönelim
                setResult(RESULT_OK);

            } else if (st instanceof UiState.Error) {
                setLoading(false);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                setLoading(false);
            }
        });
    }

    private void doLookup() {
        resetResultUi();

        String raw = etReceiverPhone.getText() != null ? etReceiverPhone.getText().toString() : "";
        String phoneE164 = PhoneUtil.normalizeTrToE164(raw);

        if (!PhoneUtil.isLikelyTrPhoneE164(phoneE164)) {
            Toast.makeText(this, "Telefon formatı geçersiz. Örn: 5xx... veya +905xx...", Toast.LENGTH_LONG).show();
            return;
        }

        lastLookupPhoneE164 = phoneE164;
        lookupOk = false;

        vm.lookupReceiver(phoneE164);
    }

    private void doCreate() {
        if (!lookupOk || lastLookupPhoneE164 == null) {
            Toast.makeText(this, "Önce kişiyi bulup onayla.", Toast.LENGTH_LONG).show();
            return;
        }

        // Kotlin’de sender_address_id default adres ile server tarafında çözülebilir.
        // Eğer siz sender_address_id göndermiyorsanız null bırakıyoruz.
        vm.createShipment(lastLookupPhoneE164, null);
    }

    private void resetLookupUi() {
        lookupOk = false;
        lastLookupPhoneE164 = null;
        tvLookupResult.setVisibility(View.GONE);
        confirmRow.setVisibility(View.GONE);
    }

    private void resetResultUi() {
        resultBox.setVisibility(View.GONE);
        tvCode.setText("Kod: -");
        tvExpires.setText("Son geçerlilik: -");
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLookup.setEnabled(!loading);
        btnConfirm.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnCopyCode.setEnabled(!loading);
    }

    private void copyCodeToClipboard() {
        String text = tvCode.getText() != null ? tvCode.getText().toString() : "";
        String code = text.replace("Kod:", "").trim();
        if (code.isEmpty() || "-".equals(code)) {
            Toast.makeText(this, "Kopyalanacak kod yok.", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("pickup_code", code));
        Toast.makeText(this, "Kod kopyalandı: " + code, Toast.LENGTH_SHORT).show();
    }
}
