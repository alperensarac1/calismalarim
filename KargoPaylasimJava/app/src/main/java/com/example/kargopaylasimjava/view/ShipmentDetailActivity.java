package com.example.kargopaylasimjava.view;

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
import com.example.kargopaylasimjava.dto.ShipmentDtos;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.util.DateUtil;
import com.example.kargopaylasimjava.viewmodel.ShipmentViewModel;

public class ShipmentDetailActivity extends ComponentActivity {

    private ShipmentViewModel vm;

    private TextView tvHeader, tvCode, tvStatus, tvCompany, tvExpire, tvExpireWarning, tvTimeline, tvAddresses;
    private Button btnRegenerate, btnDelete;
    private ProgressBar progress;

    private int shipmentId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_detail);

        shipmentId = getIntent().getIntExtra("shipment_id", 0);

        AppContainer container = new AppContainer(getApplicationContext());
        vm = new ViewModelProvider(this, new VMFactories.ShipmentVmFactory(container.repo))
                .get(ShipmentViewModel.class);

        tvHeader = findViewById(R.id.tvHeader);
        tvCode = findViewById(R.id.tvCode);
        tvStatus = findViewById(R.id.tvStatus);
        tvCompany = findViewById(R.id.tvCompany);
        tvExpire = findViewById(R.id.tvExpire);
        tvExpireWarning = findViewById(R.id.tvExpireWarning);
        tvTimeline = findViewById(R.id.tvTimeline);
        tvAddresses = findViewById(R.id.tvAddresses);

        btnRegenerate = findViewById(R.id.btnRegenerate);
        btnDelete = findViewById(R.id.btnDelete);
        progress = findViewById(R.id.progress);

        btnRegenerate.setOnClickListener(v -> vm.regenerateCode(shipmentId));
        btnDelete.setOnClickListener(v -> vm.deleteShipment(shipmentId));

        observeVm();

        if (shipmentId <= 0) {
            Toast.makeText(this, "Geçersiz gönderi", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        vm.loadDetail(shipmentId);
    }

    private void observeVm() {
        vm.detailState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);
                ShipmentDtos.ShipmentDetailDto s = (ShipmentDtos.ShipmentDetailDto) ((UiState.Success<?>) st).data;
                bindDetail(s);
            } else if (st instanceof UiState.Error) {
                setLoading(false);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                setLoading(false);
            }
        });

        vm.regenerateState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);
                ShipmentDtos.ShipmentRegenerateResp r = (ShipmentDtos.ShipmentRegenerateResp) ((UiState.Success<?>) st).data;
                Toast.makeText(this, "Kod yenilendi", Toast.LENGTH_SHORT).show();
                // detay yenile
                vm.loadDetail(shipmentId);
            } else if (st instanceof UiState.Error) {
                setLoading(false);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                setLoading(false);
            }
        });

        vm.deleteState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                setLoading(true);
            } else if (st instanceof UiState.Success) {
                setLoading(false);
                Toast.makeText(this, "Gönderi silindi", Toast.LENGTH_SHORT).show();
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

    private void bindDetail(ShipmentDtos.ShipmentDetailDto s) {
        if (s == null) return;

        tvHeader.setText(s.isSender ? "Gönderi (Gönderen)" : "Gönderi (Alıcı)");
        tvCode.setText("Kod: " + (s.pickupCode != null ? s.pickupCode : "-"));
        tvStatus.setText("Durum: " + (s.status != null ? s.status : "-"));

        String companyLine = (s.cargoCompanyName != null && !s.cargoCompanyName.trim().isEmpty())
                ? "Firma: " + s.cargoCompanyName
                : "Firma: Henüz gönderi kabul edilmemiş.";
        tvCompany.setText(companyLine);

        // expire + warning
        String expRaw = s.getCodeExpiresAt();
        String remain = DateUtil.remainingText(expRaw);
        tvExpire.setText("Geçerlilik: " + (expRaw != null ? expRaw : "-") + " (Kalan: " + remain + ")");

        boolean expired = "Süresi doldu".equals(remain);
        tvExpireWarning.setVisibility(expired ? View.VISIBLE : View.GONE);

        // Timeline
        String timeline = buildTimeline(s);
        tvTimeline.setText(timeline);

        // Addresses (detay response'unda adres alanların varsa buraya basılır,
        // şu an DTO’da yok; şimdilik boş bırakıyoruz)
        tvAddresses.setText("-");

        // delete enabled koşulu:
        // Sender ise ve gönderi kullanılmadıysa silebilsin (Kotlin’deki kuralın buysa)
        boolean canDelete = s.isSender && (s.usedAt == null || s.usedAt.trim().isEmpty());
        btnDelete.setEnabled(canDelete);

        // regenerate: sender ise ve expire geçmişse/veya her zaman aktif olsun
        btnRegenerate.setEnabled(s.isSender);
    }

    private String buildTimeline(ShipmentDtos.ShipmentDetailDto s) {
        StringBuilder sb = new StringBuilder();

        appendLine(sb, "Oluşturuldu", s.createdAt);
        appendLine(sb, "Güncellendi", s.updatedAt);
        appendLine(sb, "Onaylandı", s.confirmedAt);
        appendLine(sb, "Yolda", s.inTransitAt);
        appendLine(sb, "Teslim edildi", s.deliveredAt);
        appendLine(sb, "Kullanıldı", s.usedAt);
        appendLine(sb, "İptal edildi", s.cancelledAt);
        appendLine(sb, "Süresi doldu", s.expiredAt);

        String out = sb.toString().trim();
        return out.isEmpty() ? "-" : out;
    }

    private void appendLine(StringBuilder sb, String label, String val) {
        if (val == null || val.trim().isEmpty()) return;
        sb.append("• ").append(label).append(": ").append(val).append("\n");
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegenerate.setEnabled(!loading);
        btnDelete.setEnabled(!loading && btnDelete.isEnabled()); // mevcut kuralı bozma
    }
}
