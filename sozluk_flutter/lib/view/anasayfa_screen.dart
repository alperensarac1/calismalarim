import 'package:flutter/material.dart';
import 'package:provider/provider.dart';


import '../viewmodel/anasayfa_viewmodel.dart';
import '../widgets/search_field.dart';
import '../widgets/entry_row.dart';

class AnasayfaScreen extends StatefulWidget {
  final VoidCallback onNavigateEntryEkle;
  final void Function(int id) onNavigateEntryDetay;
  final VoidCallback onNavigateBugun;
  final VoidCallback onNavigateProfil;

  const AnasayfaScreen({
    super.key,
    required this.onNavigateEntryEkle,
    required this.onNavigateEntryDetay,
    required this.onNavigateBugun,
    required this.onNavigateProfil,
  });

  @override
  State<AnasayfaScreen> createState() => _AnasayfaScreenState();
}

class _AnasayfaScreenState extends State<AnasayfaScreen> {
  bool _loaded = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      context.read<AnaSayfaViewModel>().loadMostCommentedEntriesToday();
    }
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<AnaSayfaViewModel>();
    final ui = vm.ui;
    final entries = vm.entries;

    return Scaffold(
      appBar: AppBar(title: const Text('Gündem'), centerTitle: true),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: widget.onNavigateEntryEkle,
        label: const Text('Entry Ekle'),
        icon: const Icon(Icons.add),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: 0,
        onDestinationSelected: (i) {
          if (i == 1) widget.onNavigateBugun();
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
                  if (ui.loading) {
                    return const Center(child: CircularProgressIndicator());
                  }
                  if (ui.error != null && ui.error!.trim().isNotEmpty) {
                    return Center(
                      child: Text(
                        ui.error!,
                        style: TextStyle(color: Theme.of(context).colorScheme.error),
                      ),
                    );
                  }
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
