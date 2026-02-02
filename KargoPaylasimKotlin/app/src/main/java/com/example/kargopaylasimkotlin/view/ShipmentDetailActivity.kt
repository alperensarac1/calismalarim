package com.example.kargopaylasimkotlin.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.R
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.dto.ShipmentDetailDto
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.factory.ShipmentVmFactory
import com.example.kargopaylasimkotlin.viewmodel.ShipmentViewModel

class ShipmentDetailActivity : ComponentActivity() {

    private lateinit var vm: ShipmentViewModel
    private var shipmentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipment_detail)

        shipmentId = intent.getIntExtra("shipment_id", 0)
        if (shipmentId <= 0) {
            finish()
            return
        }

        val container = AppContainer(applicationContext)
        vm = ViewModelProvider(this, ShipmentVmFactory(container.repo))
            .get(ShipmentViewModel::class.java)

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val tvCode = findViewById<TextView>(R.id.tvCode)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvExpire = findViewById<TextView>(R.id.tvExpire)
        val tvTimeline = findViewById<TextView>(R.id.tvTimeline)
        val tvAddresses = findViewById<TextView>(R.id.tvAddresses)
        val btnRegen = findViewById<Button>(R.id.btnRegenerate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val prog = findViewById<ProgressBar>(R.id.progress)
        val tvWarn = findViewById<TextView>(R.id.tvExpireWarning)
        val tvCompany = findViewById<TextView>(R.id.tvCompany)

        // ✅ Güvenlik: detail ekranında adresler görünmesin
        tvAddresses.visibility = View.GONE

        fun isExpiredByTime(expiresAt: String?): Boolean {
            return DateUtil.remainingText(expiresAt) == "Süresi doldu"
        }

        fun render(d: ShipmentDetailDto) {
            tvHeader.text = "Gönderi #${d.id}"
            tvCode.text = "Kod: ${d.pickupCode}"
            tvStatus.text = "Durum: ${d.status}"
            tvCompany.text =
                if (d.cargoCompanyId != null && d.cargoCompanyName != "-") "Firma: ${d.cargoCompanyName}"
                else "Firma: Henüz atanmadı"


            // ✅ Hem server expire tarihini hem kalan süreyi göster
            val remaining = DateUtil.remainingText(d.codeExpiresAt)
            tvExpire.text = "Geçerlilik: ${d.codeExpiresAt}  •  Kalan: $remaining"

            val expiredByTime = isExpiredByTime(d.codeExpiresAt)
            tvWarn.visibility = if (expiredByTime) View.VISIBLE else View.GONE
            if (expiredByTime) tvWarn.text = "⚠️ Kod süresi dolmuş. Kodu yenileyin."

            // ✅ Null alanlar "-" olarak görünsün
            val tl = """
                created_at: ${d.createdAt}
                confirmed_at: ${d.confirmedAt ?: "-"}
                used_at: ${d.usedAt ?: "-"}
                in_transit_at: ${d.inTransitAt ?: "-"}
                delivered_at: ${d.deliveredAt ?: "-"}
                cancelled_at: ${d.cancelledAt ?: "-"}
                expired_at: ${d.expiredAt ?: "-"}
            """.trimIndent()
            tvTimeline.text = tl

            val canRegen =
                d.isSender &&
                        (d.status == "CREATED" || d.status == "EXPIRED") &&
                        (d.usedAt == null)

            btnRegen.isEnabled = canRegen
            btnRegen.text =
                if (expiredByTime && canRegen) "KODU YENİLE (ÖNERİLİR)"
                else "KODU YENİLE"

            val canDelete =
                d.isSender &&
                        (d.status == "CREATED" || d.status == "EXPIRED" || d.status == "CANCELLED") &&
                        (d.usedAt == null)

            btnDelete.visibility = if (d.isSender) View.VISIBLE else View.GONE
            btnDelete.isEnabled = canDelete
        }

        btnRegen.setOnClickListener {
            vm.regenerateCode(shipmentId)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Gönderi silinsin mi?")
                .setMessage("Bu işlem geri alınamaz. Devam edilsin mi?")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Sil") { _, _ ->
                    vm.deleteShipment(shipmentId)
                }
                .show()
        }

        vm.detailState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    render(st.data)
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        vm.regenerateState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, "Yeni kod: ${st.data.pickup_code}", Toast.LENGTH_LONG).show()
                    vm.loadDetail(shipmentId)
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // ✅ Delete observer (ViewModel'e eklediğimiz deleteState)
        vm.deleteState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, "Gönderi silindi", Toast.LENGTH_LONG).show()
                    finish()
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                    println(st.message)
                }
                else -> Unit
            }
        }

        vm.loadDetail(shipmentId)
    }
}
