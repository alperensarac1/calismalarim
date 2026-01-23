package com.example.csvexplorerjetpack.navigation

import com.example.csvexplorerjetpack.viewmodel.MainViewModel


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNav(vm: MainViewModel, onPickCsv: () -> Unit) {
    val nav = rememberNavController()
    val selectedJson = remember { mutableStateOf<String?>(null) }

    NavHost(navController = nav, startDestination = "main") {
        composable("main") {
            MainScreen(
                vm = vm,
                onPickCsv = onPickCsv,
                onOpenDetails = { json ->
                    selectedJson.value = json
                    nav.navigate("details")
                }
            )
        }
        composable("details") {
            DetailsScreen(
                json = selectedJson.value ?: "{}",
                headers = vm.state.value.headers,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
