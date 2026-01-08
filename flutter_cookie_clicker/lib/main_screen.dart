import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';


import 'controller/game_controller.dart';
import 'model/floating_text.dart';
import 'model/upgrade.dart';
import 'prestige_shop_screen.dart';

class MainScreen extends ConsumerStatefulWidget {
  const MainScreen({super.key});

  @override
  ConsumerState<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends ConsumerState<MainScreen>
    with SingleTickerProviderStateMixin {
  late final AnimationController _cookieCtl;
  late final Animation<double> _cookieScale;

  @override
  void initState() {
    super.initState();
    _cookieCtl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 120),
      lowerBound: 0.92,
      upperBound: 1.0,
      value: 1.0,
    );
    _cookieScale = _cookieCtl;
  }

  @override
  void dispose() {
    _cookieCtl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = ref.watch(gameControllerProvider);
    final game = s.game;
    final perks = s.perks;
    final upgrades = s.upgrades;
    final floaters = s.floaters;
    final critReady = s.critReady;
    final critLeft = s.critCooldownLeft;

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFFFFF3E5),
              Color(0xFFFFE0B2),
            ],
          ),
        ),
        child: SafeArea(
          child: Stack(
            children: [
              Column(
                children: [
                  // Üst kart
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Card(
                      elevation: 1,
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              formatNum(game.score),
                              style: Theme.of(context)
                                  .textTheme
                                  .headlineMedium
                                  ?.copyWith(fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              "${formatNum(game.cps)} / sn",
                              style: TextStyle(color: Colors.grey.shade800),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),

                  // Cookie alanı (tapDown ile x/y alıyoruz)
                  SizedBox(
                    height: 300,
                    child: Center(
                      child: GestureDetector(
                        onTapDown: (d) async {
                          // anim
                          _cookieCtl.value = 0.92;
                          _cookieCtl.animateTo(1.0,
                              curve: Curves.easeOut);

                          final p = d.localPosition;
                          await ref
                              .read(gameControllerProvider.notifier)
                              .onTapCookie(p.dx, p.dy);
                        },
                        child: ScaleTransition(
                          scale: _cookieScale,
                          child: Stack(
                            alignment: Alignment.center,
                            children: const [
                              Icon(Icons.circle,
                                  size: 220, color: Color(0xFFCE9B62)),
                              Text("🍪", style: TextStyle(fontSize: 64)),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ),

                  // Upgrades list
                  Expanded(
                    child: ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: upgrades.length + 1,
                      itemBuilder: (context, i) {
                        if (i == 0) {
                          return Padding(
                            padding: const EdgeInsets.symmetric(vertical: 6),
                            child: Text(
                              "Yükseltmeler",
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                          );
                        }

                        final u = upgrades[i - 1];
                        final price = u.currentPrice() * (1.0 - _discountPct(perks.discount));
                        final canAfford = game.score >= price;

                        return UpgradeRow(
                          upgrade: u,
                          price: price,
                          canAfford: canAfford,
                          onBuy: () => ref
                              .read(gameControllerProvider.notifier)
                              .buyUpgrade(u),
                        );
                      },
                    ),
                  ),

                  // Bottom bar
                  Container(
                    height: 64,
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    decoration:
                    BoxDecoration(color: Colors.white.withOpacity(0.13)),
                    child: Row(
                      children: [
                        Expanded(
                          child: ElevatedButton(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => PrestigeShopScreen(
                                    onBack: () => Navigator.pop(context),
                                  ),
                                ),
                              );
                            },
                            child: const Text("Shop"),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: OutlinedButton(
                            onPressed: () => ref
                                .read(gameControllerProvider.notifier)
                                .prestige(),
                            child: const Text("Prestige"),
                          ),
                        ),
                        const SizedBox(width: 8),
                        OutlinedButton(
                          onPressed: () => ref
                              .read(gameControllerProvider.notifier)
                              .reset(),
                          child: const Text("Reset"),
                        ),
                        const SizedBox(width: 8),
                        OutlinedButton(
                          onPressed: critReady
                              ? () => ref
                              .read(gameControllerProvider.notifier)
                              .doCrit(110, 110)
                              : null,
                          child: Text(critReady ? "Crit" : "${critLeft}s"),
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              // Floating overlay
              ...floaters.map((f) => FloatingTextBubble(f)).toList(),
            ],
          ),
        ),
      ),
    );
  }
}

double _discountPct(int discountLevel) {
  final v = discountLevel * 0.02;
  return v > 0.50 ? 0.50 : v;
}

String formatNum(double v) {
  if (v >= 1_000_000) return "${(v / 1_000_000).toStringAsFixed(2)}M";
  if (v >= 1_000) return "${(v / 1_000).toStringAsFixed(1)}k";
  return v.toStringAsFixed(0);
}

class FloatingTextBubble extends StatelessWidget {
  final FloatingText f;
  const FloatingTextBubble(this.f, {super.key});

  @override
  Widget build(BuildContext context) {
    // Compose'daki dp mantığına yakın: direkt px gibi kullanıyoruz.
    final top = max(0.0, f.y - 120);

    return Positioned(
      left: f.x,
      top: top,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
        decoration: BoxDecoration(
          color: Colors.black.withOpacity(0.4),
          borderRadius: BorderRadius.circular(4),
        ),
        child: Text(
          f.text,
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: f.isCrit ? Colors.red : Colors.white,
          ),
        ),
      ),
    );
  }
}

class UpgradeRow extends StatelessWidget {
  final Upgrade upgrade;
  final double price;
  final bool canAfford;
  final VoidCallback onBuy;

  const UpgradeRow({
    super.key,
    required this.upgrade,
    required this.price,
    required this.canAfford,
    required this.onBuy,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1,
      margin: const EdgeInsets.symmetric(vertical: 6),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Icon(_iconFromName(upgrade.icon),
                size: 32, color: const Color(0xFF795548)),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    upgrade.level > 0
                        ? "${upgrade.title} (Lv ${upgrade.level})"
                        : upgrade.title,
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 2),
                  Text(upgrade.desc, style: TextStyle(color: Colors.grey.shade800)),
                ],
              ),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(formatNum(price)),
                const SizedBox(height: 6),
                OutlinedButton(
                  onPressed: canAfford ? onBuy : null,
                  child: const Text("Buy"),
                ),
              ],
            )
          ],
        ),
      ),
    );
  }
}

IconData _iconFromName(String name) {
  switch (name) {
    case "Bolt":
      return Icons.bolt;
    case "FastForward":
      return Icons.fast_forward;
    case "LocalFireDepartment":
      return Icons.local_fire_department;
    case "GridView":
      return Icons.grid_view;
    case "Store":
      return Icons.store;
    case "Factory":
      return Icons.factory;
    case "Science":
      return Icons.science;
    case "Rocket":
      return Icons.rocket;
    default:
      return Icons.star;
  }
}
