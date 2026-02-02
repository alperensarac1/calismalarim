package com.example.kargopaylasimkotlin.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kargopaylasimkotlin.R
import com.example.kargopaylasimkotlin.adapter.AddressAdapter
import com.example.kargopaylasimkotlin.adapter.ShipmentAdapter
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.factory.AddressListVmFactory
import com.example.kargopaylasimkotlin.factory.ShipmentVmFactory
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.viewmodel.AddressListViewModel
import com.example.kargopaylasimkotlin.viewmodel.ShipmentViewModel

class HomeActivity : ComponentActivity() {

    private lateinit var vm: ShipmentViewModel
    private lateinit var adapter: ShipmentAdapter

    private lateinit var addrVm: AddressListViewModel
    private lateinit var addrAdapter: AddressAdapter

    private val createShipmentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                vm.loadShipments()
            }
        }

    private val editAddressLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                addrVm.load()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val container = AppContainer(applicationContext)
        if (!container.tokenStore.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Shipments VM
        vm = ViewModelProvider(this, ShipmentVmFactory(container.repo))
            .get(ShipmentViewModel::class.java)

        // Addresses VM
        addrVm = ViewModelProvider(this, AddressListVmFactory(container.repo))
            .get(AddressListViewModel::class.java)

        val btnNew = findViewById<Button>(R.id.btnNew)
        val btnAddress = findViewById<Button>(R.id.btnAddress)

        // Shipments UI
        val rvShip = findViewById<RecyclerView>(R.id.rvShipments)
        val prog = findViewById<ProgressBar>(R.id.progress)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // Addresses UI
        val rvAddr = findViewById<RecyclerView>(R.id.rvAddresses)
        val tvAddrEmpty = findViewById<TextView>(R.id.tvAddrEmpty)
        val swShip = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swShipments)

        adapter = ShipmentAdapter(emptyList()) { item ->
            val i = Intent(this, ShipmentDetailActivity::class.java)
            i.putExtra("shipment_id", item.id)
            startActivity(i)
        }
        rvShip.layoutManager = LinearLayoutManager(this)
        rvShip.adapter = adapter

        // Address adapter
        addrAdapter = AddressAdapter(
            onEdit = { a ->
                val i = Intent(this, EditAddressActivity::class.java)
                i.putExtra("address_id", a.id)
                // editAddressLauncher ile açarsan kaydedince refresh garanti olur
                editAddressLauncher.launch(i)
            },
            onSetDefault = { a -> addrVm.setDefault(a.id) },
            onDelete = { a -> addrVm.delete(a.id) }
        )
        rvAddr.layoutManager = LinearLayoutManager(this)
        rvAddr.adapter = addrAdapter

        // ✅ yeni kargo oluşturma: launcher ile aç
        btnNew.setOnClickListener {
            createShipmentLauncher.launch(Intent(this, CreateShipmentActivity::class.java))
        }
        swShip.setOnRefreshListener {
            vm.loadShipments()
        }
        // ✅ adres ekranı (yeni adres/create)
        btnAddress.setOnClickListener {
            editAddressLauncher.launch(Intent(this, EditAddressActivity::class.java))
        }

        vm.listState.observe(this) { st ->
            if (swShip.isRefreshing) swShip.isRefreshing = false

            when (st) {
                is UiState.Loading -> {
                    prog.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                }
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    adapter.submit(st.data)
                    tvEmpty.visibility = if (st.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // Addresses observer
        addrVm.listState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> {
                    tvAddrEmpty.visibility = View.GONE
                }
                is UiState.Success -> {
                    val list = st.data
                    addrAdapter.submit(list)
                    tvAddrEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
                is UiState.Error -> {
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        addrVm.setDefaultState.observe(this) { st ->
            when (st) {
                is UiState.Success -> addrVm.load()
                is UiState.Error -> Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }

        addrVm.deleteState.observe(this) { st ->
            when (st) {
                is UiState.Success -> addrVm.load()
                is UiState.Error -> Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }

        // ✅ ilk açılışta yükle
        vm.loadShipments()
        addrVm.load()
    }

    override fun onResume() {
        super.onResume()
        // istersen kalsın; activityResult zaten garanti refresh yapacak
        vm.loadShipments()
        addrVm.load()
    }
}
