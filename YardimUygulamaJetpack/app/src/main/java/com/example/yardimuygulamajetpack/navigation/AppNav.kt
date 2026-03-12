package com.example.yardimuygulamajetpack.navigation



import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

sealed class Route(val r: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Patient : Route("patient")
    data object HelperOpen : Route("helper_open")
    data object HelperAccepted : Route("helper_accepted")
    data object HelperHistory : Route("helper_history")
}

@Composable
fun AppNav(start: String) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = start) {
        composable(Route.Login.r) { LoginScreen(nav) }
        composable(Route.Register.r) { RegisterScreen(nav) }
        composable(Route.Patient.r) { PatientScreen(nav) }
        composable(Route.HelperOpen.r) { HelperOpenScreen(nav) }
        composable(Route.HelperAccepted.r) { HelperAcceptedScreen(nav) }
        composable(Route.HelperHistory.r) { HelperHistoryScreen(nav) }
    }
}