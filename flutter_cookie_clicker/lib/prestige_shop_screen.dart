import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'controller/game_controller.dart';
import 'model/perk_ui.dart';


class PrestigeShopScreen extends ConsumerWidget {
  final VoidCallback onBack;
  const PrestigeShopScreen({super.key, required this.onBack});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = ref.watch(gameControllerProvider);
    final perks = s.perks;

    final items = <PerkUi>[
      PerkUi(
        key: "gprod",
        title: "Altın Çırpıcı",
        desc: "%5 üretim çarpanı / seviye (CPS & tap)",
        baseCost: 1,
        scaling: 1.6,
        level: perks.gprod,
      ),
      PerkUi(
        key: "crit",
        title: "Uğurlu Tılsım",
        desc: "%1 pasif crit şansı / seviye (tap x3)",
        baseCost: 2,
        scaling: 1.7,
        level: perks.crit,
      ),
      PerkUi(
        key: "discount",
        title: "Toplu Alım",
        desc: "Upgrade fiyatlarında %2 indirim / seviye (maks %50)",
        baseCost: 3,
        scaling: 1.8,
        level: perks.discount,
        maxLevel: 25,
      ),
      PerkUi(
        key: "tapTop",
        title: "Turbo Tap",
        desc: "Kalıcı +1 tap gücü / seviye",
        baseCost: 2,
        scaling: 1.5,
        level: perks.tapTop,
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text("Prestige Mağazası"),
        leading: TextButton(
          onPressed: onBack,
          child: const Text("Geri"),
        ),
        leadingWidth: 80,
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "Prestige Puanı: ${perks.points}",
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
          ),
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: items.length,
              itemBuilder: (context, i) {
                final p = items[i];
                final cost = p.costForNext();
                final canBuy = perks.points >= cost && p.level < p.maxLevel;

                return Card(
                  margin: const EdgeInsets.symmetric(vertical: 6),
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          "${p.title} (Lv ${p.level})",
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          p.desc,
                          style: TextStyle(color: Colors.grey.shade700),
                        ),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            Expanded(child: Text("Maliyet: $cost")),
                            ElevatedButton(
                              onPressed: canBuy
                                  ? () {
                                ref
                                    .read(gameControllerProvider.notifier)
                                    .buyPerk(p.key, cost, maxLevel: p.maxLevel);
                              }
                                  : null,
                              child: const Text("Satın Al"),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
