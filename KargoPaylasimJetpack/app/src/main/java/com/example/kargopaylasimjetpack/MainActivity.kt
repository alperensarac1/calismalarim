package com.example.kargopaylasimjetpack


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kargopaylasimjetpack.di.AppContainer
import com.example.kargopaylasimjetpack.navigation.Routes
import com.example.kargopaylasimjetpack.view.CreateAddressScreen
import com.example.kargopaylasimjetpack.view.CreateShipmentScreen
import com.example.kargopaylasimjetpack.view.HomeScreen
import com.example.kargopaylasimjetpack.view.LoginScreen
import com.example.kargopaylasimjetpack.view.RegisterScreen
import com.example.kargopaylasimjetpack.viewmodel.AddressCreateVM
import com.example.kargopaylasimjetpack.viewmodel.AuthVM
import com.example.kargopaylasimjetpack.viewmodel.CreateShipmentVM
import com.example.kargopaylasimjetpack.viewmodel.HomeVM

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AppContainer(applicationContext)

        setContent {
            val nav = rememberNavController()

            // basit authed kontrol
            val start = remember {
                val token = runBlocking { container.tokenStore.tokenFlow.first() }
                if (token.isNullOrBlank()) Routes.LOGIN else Routes.HOME
            }

            NavHost(navController = nav, startDestination = start) {

                composable(Routes.LOGIN) {
                    val vm = remember { AuthVM(container.repo, container.tokenStore) }
                    LoginScreen(
                        vm = vm,
                        onGoRegister = { nav.navigate(Routes.REGISTER) },
                        onLoggedIn = {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    val vm = remember { AuthVM(container.repo, container.tokenStore) }
                    RegisterScreen(vm = vm, onBack = { nav.popBackStack() })
                }

                composable(Routes.HOME) {
                    val vm = remember { HomeVM(container.repo) }
                    HomeScreen(
                        vm = vm,
                        onGoCreateShipment = { nav.navigate(Routes.CREATE_SHIPMENT) },
                        onGoCreateAddress = { nav.navigate(Routes.CREATE_ADDRESS) }
                    )
                }

                composable(Routes.CREATE_SHIPMENT) {
                    val vm = remember { CreateShipmentVM(container.repo) }
                    CreateShipmentScreen(vm = vm, onDone = { nav.popBackStack() })
                }

                composable(Routes.CREATE_ADDRESS) {
                    val vm = remember { AddressCreateVM(container.repo) }
                    CreateAddressScreen(vm = vm, onDone = { nav.popBackStack() })
                }
            }
        }
    }
}
