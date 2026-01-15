import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../components/snackbar.dart';
import '../viewmodel/oda_vm.dart';

class AnasayfaScreen extends StatefulWidget {
  final int userId;
  final void Function(int roomId, int userId) onOpenRoom;

  const AnasayfaScreen({
    super.key,
    required this.userId,
    required this.onOpenRoom,
  });

  @override
  State<AnasayfaScreen> createState() => _AnasayfaScreenState();
}

class _AnasayfaScreenState extends State<AnasayfaScreen> {
  @override
  void initState() {
    super.initState();
    // Compose LaunchedEffect(userId)
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<OdaVM>().fetchJoinedRooms(widget.userId);
    });
  }

  Future<void> _showJoinDialog(BuildContext context) async {
    final codeCtrl = TextEditingController();

    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Oda Katılım'),
        content: TextField(
          controller: codeCtrl,
          decoration: const InputDecoration(labelText: 'Oda Kodu'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('İptal')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Katıl'),
          ),
        ],
      ),
    );

    if (ok == true) {
      final code = codeCtrl.text.trim();
      if (code.isEmpty) return;

      final vm = context.read<OdaVM>();
      await vm.joinRoom(userId: widget.userId, roomCode: code);

      if (!mounted) return;

      if (vm.joinResult?.success == true) {
        showSnack(context, 'Odaya katıldınız');
        await vm.fetchJoinedRooms(widget.userId);
      } else {
        showSnack(context, vm.joinResult?.message ?? vm.error ?? 'Katılım başarısız');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<OdaVM>(
      builder: (context, vm, _) {
        final rooms = vm.joinedRooms;

        return Scaffold(
          appBar: AppBar(title: const Text('Odalarım')),
          floatingActionButton: FloatingActionButton.extended(
            onPressed: vm.isLoadingCreate
                ? null
                : () async {
              await vm.createRoom(userId: widget.userId);

              if (!mounted) return;

              if (vm.odaOlusturmaSonucu?.success == true) {
                showSnack(context, 'Oda oluşturuldu: ${vm.odaOlusturmaSonucu?.roomCode ?? ""}');
                await vm.fetchJoinedRooms(widget.userId);
              } else {
                showSnack(context, vm.odaOlusturmaSonucu?.message ?? vm.error ?? 'Oda oluşturma hatası');
              }
            },
            label: vm.isLoadingCreate ? const Text('...') : const Text('Oda Oluştur'),
          ),
          body: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () => _showJoinDialog(context),
                    child: const Text('Odaya Katıl (Kod ile)'),
                  ),
                ),
                const SizedBox(height: 12),
                Expanded(
                  child: vm.isLoadingRooms
                      ? const Center(child: CircularProgressIndicator())
                      : rooms.isEmpty
                      ? const Center(child: Text('Henüz oda yok'))
                      : ListView.separated(
                    itemCount: rooms.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (ctx, i) {
                      final oda = rooms[i];
                      return Card(
                        child: ListTile(
                          title: Text(oda.roomCode),
                          subtitle: Text('Oluşturan: ${oda.createdBy}'),
                          onTap: () => widget.onOpenRoom(oda.odaId, widget.userId),
                        ),
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
