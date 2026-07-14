package com.alperensarac.ebiletjetpack.navigation

/*
    Routes

    Navigation Compose ekran isimlerini tek yerden yönetir.

    Böylece string hatası yapma riskimiz azalır.
*/
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    /*
        Parametreli ekranlar:
        event_detail/{eventId}
        ticket_detail/{ticketId}
    */
    const val EVENT_DETAIL = "event_detail"
    const val MY_TICKETS = "my_tickets"
    const val TICKET_DETAIL = "ticket_detail"
    const val SCANNER = "scanner"
}