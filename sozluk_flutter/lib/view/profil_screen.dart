import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../entity/session_manager.dart';
import '../viewmodel/profil_viewmodel.dart';
import '../widgets/search_field.dart';

class ProfilScreen extends StatefulWidget {
  final SessionManager session;
  final VoidCallback onNavigateGundem;
  final VoidCallback onNavigateBugun;
  final void Function(int entryId) onNavigateEntryDetay;
  final Future<void> Function() onLoggedOut;

  const ProfilScreen({
    super.key,
    required this.session,
    required this.onNavigateGundem,
    required this.onNavigateBugun,
    required this.onNavigateEntryDetay,
    required this.onLoggedOut,
  });

  @override
  State<ProfilScreen> createState() => _ProfilScreenState();
}

class _ProfilScreenState extends State<ProfilScreen> {
  bool _loaded = false;
  int _userId = -1;
  String _username = 'Bilinmeyen Kullanıcı';

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      _init();
    }
  }

  Future<void> _init() async {
    final uid = await widget.session.getUserId();
    final uname = await widget.session.getUsername();
    if (!mounted) return;

    setState(() {
      _userId = uid;
      _username = uname ?? 'Bilinmeyen Kullanıcı';
    });

    await context.read<ProfilViewModel>().loadUserEntries(uid);
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<ProfilViewModel>();
    final ui = vm.ui;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final res = vm.deleteResult;
      if (res == null) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(res.message ?? (res.success ? 'Silindi' : 'Silinemedi'))),
      );
    });

    return Scaffold(
      appBar: AppBar(
        title: Text(_username),
        centerTitle: true,
        actions: [
          TextButton(
            onPressed: () => _showLogoutDialog(context),
            child: const Text('Çıkış Yap'),
          )
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: 2,
        onDestinationSelected: (i) {
          if (i == 0) widget.onNavigateGundem();
          if (i == 1) widget.onNavigateBugun();
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
                    itemBuilder: (_, i) {
                      final e = entries[i];
                      return Card(
                        elevation: 1,
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Row(
                            children: [
                              Expanded(
                                child: InkWell(
                                  onTap: () => widget.onNavigateEntryDetay(e.id),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(e.title, style: Theme.of(context).textTheme.titleMedium),
                                      const SizedBox(height: 6),
                                      Text(e.content, maxLines: 2, overflow: TextOverflow.ellipsis),
                                    ],
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                              OutlinedButton(
                                onPressed: () {
                                  context.read<ProfilViewModel>().deleteEntry(
                                    entryId: e.id,
                                    userId: _userId,
                                  );
                                },
                                child: const Text('Sil'),
                              ),
                            ],
                          ),
                        ),
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

  Future<void> _showLogoutDialog(BuildContext context) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Çıkış Yap'),
        content: const Text('Oturumunuzu kapatmak istediğinizden emin misiniz?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('İptal')),
          TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('Evet')),
        ],
      ),
    );

    if (ok == true) {
      await widget.session.clearSession();
      await widget.onLoggedOut();
    }
  }
}
