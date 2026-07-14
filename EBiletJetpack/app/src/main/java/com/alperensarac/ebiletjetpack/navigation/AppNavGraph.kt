package com.alperensarac.ebiletjetpack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alperensarac.ebiletjetpack.ui.auth.LoginScreen
import com.alperensarac.ebiletjetpack.ui.auth.RegisterScreen
import com.alperensarac.ebiletjetpack.ui.event.EventDetailScreen
import com.alperensarac.ebiletjetpack.ui.home.HomeScreen
import com.alperensarac.ebiletjetpack.ui.scanner.TicketScannerScreen
import com.alperensarac.ebiletjetpack.ui.ticket.MyTicketsScreen
import com.alperensarac.ebiletjetpack.ui.ticket.TicketDetailScreen

/*
    AppNavGraph

    Artık tüm temel ekranlarımız gerçek:

    - LoginScreen
    - RegisterScreen
    - HomeScreen
    - EventDetailScreen
    - MyTicketsScreen
    - TicketDetailScreen
    - TicketScannerScreen
*/
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                },
                onOpenMyTickets = {
                    navController.navigate(Routes.MY_TICKETS)
                },
                onOpenScanner = {
                    navController.navigate(Routes.SCANNER)
                },
                onOpenEventDetail = { eventId ->
                    navController.navigate("${Routes.EVENT_DETAIL}/$eventId")
                }
            )
        }

        composable("${Routes.EVENT_DETAIL}/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments
                ?.getString("eventId")
                ?.toIntOrNull()
                ?: 0

            EventDetailScreen(
                eventId = eventId,
                onBack = {
                    navController.popBackStack()
                },
                onTicketBought = {
                    navController.navigate(Routes.MY_TICKETS)
                }
            )
        }

        composable(Routes.MY_TICKETS) {
            MyTicketsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenTicketDetail = { ticketId ->
                    navController.navigate("${Routes.TICKET_DETAIL}/$ticketId")
                }
            )
        }

        composable("${Routes.TICKET_DETAIL}/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments
                ?.getString("ticketId")
                ?.toIntOrNull()
                ?: 0

            TicketDetailScreen(
                ticketId = ticketId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        /*
            Gerçek QR kontrol ekranı.
        */
        composable(Routes.SCANNER) {
            TicketScannerScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}