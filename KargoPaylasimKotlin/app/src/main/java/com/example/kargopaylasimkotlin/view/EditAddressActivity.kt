package com.example.kargopaylasimkotlin.view

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.R
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.dto.AddressUpdateReq
import com.example.kargopaylasimkotlin.factory.AddressVmFactory
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.viewmodel.AddressViewModel


class EditAddressActivity : ComponentActivity() {

    private lateinit var vm: AddressViewModel
    private var addressId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_address)

        addressId = intent.getIntExtra("address_id", 0)

        val container = AppContainer(applicationContext)
        vm = ViewModelProvider(this, AddressVmFactory(container.repo))
            .get(AddressViewModel::class.java)

        // UI
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etDistrict = findViewById<EditText>(R.id.etDistrict)
        val etNeighborhood = findViewById<EditText>(R.id.etNeighborhood)
        val etAddressLine = findViewById<EditText>(R.id.etLine)
        val etPostal = findViewById<EditText>(R.id.etPostal)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Edit mod ise detayı çek
        if (addressId > 0) {
            vm.loadById(addressId)
        }

        vm.defaultState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> {
                    btnSave.isEnabled = false
                }
                is UiState.Success -> {
                    btnSave.isEnabled = true
                    val a = st.data

                    if (addressId > 0 && a.id == addressId) {
                        etTitle.setText(a.title ?: "")
                        etCity.setText(a.city ?: "")
                        etDistrict.setText(a.district ?: "")
                        etNeighborhood.setText(a.neighborhood ?: "")
                        etAddressLine.setText(a.address_line ?: "")
                        etPostal.setText(a.postal_code ?: "")
                    }
                }
                is UiState.Error -> {
                    btnSave.isEnabled = true
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    btnSave.isEnabled = true
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val district = etDistrict.text.toString().trim()
            val neigh = etNeighborhood.text.toString().trim()
            val line = etAddressLine.text.toString().trim()
            val postal = etPostal.text.toString().trim()

            if (title.isEmpty() || city.isEmpty() || district.isEmpty() || line.isEmpty()) {
                Toast.makeText(this, "Başlık/Şehir/İlçe/Adres zorunlu", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            vm.saveOrCreate(
                addressId = addressId,
                title = title,
                city = city,
                district = district,
                neighborhood = neigh,
                line = line,
                postalCode = postal
            )
        }

        vm.saveState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> {
                    btnSave.isEnabled = false
                    btnSave.text = "Kaydediliyor..."
                }
                is UiState.Success -> {
                    Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is UiState.Error -> {
                    btnSave.isEnabled = true
                    btnSave.text = "Kaydet"
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    btnSave.isEnabled = true
                    btnSave.text = "Kaydet"
                }
            }
        }
    }
}
