package com.example.kargopaylasimkotlin.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kargopaylasimkotlin.R
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.factory.ShipmentVmFactory
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.util.PhoneUtil
import com.example.kargopaylasimkotlin.viewmodel.ShipmentViewModel
import com.google.android.material.card.MaterialCardView

class CreateShipmentActivity : ComponentActivity() {

    private lateinit var vm: ShipmentViewModel
    private var confirmedPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shipment)

        val container = AppContainer(applicationContext)
        vm = ViewModelProvider(this, ShipmentVmFactory(container.repo))
            .get(ShipmentViewModel::class.java)

        val etPhone = findViewById<EditText>(R.id.etReceiverPhone)
        val btnLookup = findViewById<Button>(R.id.btnLookup)
        val tvRes = findViewById<TextView>(R.id.tvLookupResult)
        val confirmRow = findViewById<LinearLayout>(R.id.confirmRow)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val prog = findViewById<ProgressBar>(R.id.progress)

        val resultBox = findViewById<MaterialCardView>(R.id.resultBox)
        val tvCode = findViewById<TextView>(R.id.tvCode)
        val tvExpires = findViewById<TextView>(R.id.tvExpires)
        val btnCopy = findViewById<Button>(R.id.btnCopyCode)
        btnCopy.visibility = View.GONE
        fun resetConfirm() {
            confirmedPhone = null
            tvRes.visibility = View.GONE
            confirmRow.visibility = View.GONE
            resultBox.visibility = View.GONE
        }
        btnLookup.setOnClickListener {
            resetConfirm()
            val phoneRaw = etPhone.text.toString()
            val phone = PhoneUtil.normalizeTrToE164(phoneRaw)

            if (!PhoneUtil.isLikelyTrPhoneE164(phone)) {
                Toast.makeText(this, "Telefon formatı hatalı. Örn: 05xx... veya +905xx...", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            etPhone.setText(phone) // kullanıcı ekranda da görsün
            vm.lookupReceiver(phone)
        }

        btnLookup.setOnClickListener {
            resetConfirm()
            val phone = etPhone.text.toString().trim()
            if (phone.length < 10) {
                Toast.makeText(this, "Telefonu kontrol et.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            vm.lookupReceiver(phone)
        }

        btnCancel.setOnClickListener { resetConfirm() }

        btnConfirm.setOnClickListener {
            val phone = confirmedPhone
            if (phone == null) {
                Toast.makeText(this, "Önce kişiyi bul.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // senderAddressId şimdilik null: backend default adresi kullanır
            vm.createShipment(phone, null)
        }

        vm.lookupState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    val d = st.data
                    tvRes.text = "Bulunan: ${d.masked_first_name} ${d.masked_last_name}  • Onaylıyor musun?"
                    tvRes.visibility = View.VISIBLE
                    confirmRow.visibility = View.VISIBLE
                    confirmedPhone = etPhone.text.toString().trim()
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                    if (st.message.contains("Receiver address not found", true)) {
                        Toast.makeText(this, "Bu kullanıcı henüz adresini kaydetmemiş.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                    }
                    prog.visibility = View.GONE

                }
                else -> Unit
            }
        }

        vm.createState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    val d = st.data
                    resultBox.visibility = View.VISIBLE
                    tvCode.text = "Kod: ${d.pickup_code}"
                    tvExpires.text = "Son geçerlilik: ${d.code_expires_at}"
                    confirmRow.visibility = View.GONE
                    setResult(RESULT_OK)   // ✅ Home refresh tetikler
                    finish()
                    btnCopy.visibility = View.INVISIBLE
                    btnCopy.setOnClickListener {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("pickup_code", d.pickup_code))
                        Toast.makeText(this, "Kod kopyalandı", Toast.LENGTH_SHORT).show()
                    }
                }
                is UiState.Error -> {
                    if (st.message.contains("Receiver address not found", true)) {
                        Toast.makeText(this, "Bu kullanıcı henüz adresini kaydetmemiş.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                    }
                    prog.visibility = View.GONE
                }
                else -> Unit
            }
        }

    }
}
