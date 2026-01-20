import 'package:eticaret_flutter/settings/settings_screen.dart';
import 'package:flutter/material.dart';

import 'cart/cart_page.dart';
import 'home/home_page.dart';
import 'orders/orders_page.dart';


class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int index = 0;

  final pages = const [
    HomePage(),
    CartPage(),
    OrdersPage(),
    SettingsPage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: pages[index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (i) => setState(() => index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home), label: "Home"),
          NavigationDestination(icon: Icon(Icons.shopping_cart), label: "Cart"),
          NavigationDestination(icon: Icon(Icons.receipt_long), label: "Orders"),
          NavigationDestination(icon: Icon(Icons.settings), label: "Settings"),
        ],
      ),
    );
  }
}
