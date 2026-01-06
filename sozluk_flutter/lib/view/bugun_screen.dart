import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodel/bugun_viewmodel.dart';
import '../widgets/search_field.dart';
import '../widgets/entry_row.dart';

class BugunScreen extends StatefulWidget {
  final VoidCallback onNavigateGundem;
  final VoidCallback onNavigateProfil;
  final void Function(int entryId) onNavigateEntryDetay;

  const BugunScreen({
    super.key,
    required this.onNavigateGundem,
    required this.onNavigateProfil,
    required this.onNavigateEntryDetay,
  });

  @override
  State<BugunScreen> createState() => _BugunScreenState();
}

class _BugunScreenState extends State<BugunScreen> {
  bool _loaded = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      context.read<BugunViewModel>().loadTodayEntries();
    }
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<BugunViewModel>();
    final ui = vm.ui;

    return Scaffold(
      appBar: AppBar(title: const Text('Bugün'), centerTitle: true),
      bottomNavigationBar: NavigationBar(
        selectedIndex: 1,
        onDestinationSelected: (i) {
          if (i == 0) widget.onNavigateGundem();
          if (i == 2) widget.onNavigateProfil();
        },
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home), label: 'Gündem'),
          NavigationDestination(icon: Icon(Icons.today), label: 'Bugün'),
          NavigationDestination(icon: Icon(Icons.person), label: 'Profil'),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            SearchField(value: vm.searchQuery, onChanged: vm.setSearchQuery),
            const SizedBox(height: 12),
            Expanded(
              child: Builder(
                builder: (_) {
                  if (ui.loading) return const Center(child: CircularProgressIndicator());
                  if (ui.error != null && ui.error!.trim().isNotEmpty) {
                    return Center(
                      child: Text(
                        ui.error!,
                        style: TextStyle(color: Theme.of(context).colorScheme.error),
                      ),
                    );
                  }

                  final entries = vm.entries;
                  return ListView.separated(
                    itemCount: entries.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (_, idx) {
                      final e = entries[idx];
                      return EntryRow(
                        entry: e,
                        onTap: () => widget.onNavigateEntryDetay(e.id),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
