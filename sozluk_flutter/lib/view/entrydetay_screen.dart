import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../entity/session_manager.dart';
import '../model/comment.dart';
import '../viewmodel/entrydetay_viewmodel.dart';


class EntryDetayScreen extends StatefulWidget {
  final int entryId;
  final SessionManager session;
  final VoidCallback onBack;

  const EntryDetayScreen({
    super.key,
    required this.entryId,
    required this.session,
    required this.onBack,
  });

  @override
  State<EntryDetayScreen> createState() => _EntryDetayScreenState();
}

class _EntryDetayScreenState extends State<EntryDetayScreen> {
  bool _loaded = false;
  final _commentCtrl = TextEditingController();

  @override
  void dispose() {
    _commentCtrl.dispose();
    super.dispose();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_loaded) {
      _loaded = true;
      final vm = context.read<EntryDetayViewModel>();
      vm.loadEntry(widget.entryId);
      vm.loadComments(widget.entryId);
    }
  }

  Future<int> _userId() => widget.session.getUserId();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<EntryDetayViewModel>();
    final ui = vm.ui;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final err = ui.error;
      if (err != null && err.trim().isNotEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(err)));
      }
    });

    final entry = vm.entry;

    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: Text(entry?.title ?? 'Entry Detay'),
        leading: TextButton(onPressed: widget.onBack, child: const Text('Geri')),
        leadingWidth: 72,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (ui.loadingEntry)
              const Center(child: CircularProgressIndicator())
            else if (entry != null)
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(entry.title, style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 6),
                  Text(entry.content, style: Theme.of(context).textTheme.bodyLarge),
                  const SizedBox(height: 6),
                  Text(
                    [
                      entry.username,
                      entry.createdAt.isNotEmpty ? entry.createdAt.substring(0, entry.createdAt.length >= 10 ? 10 : entry.createdAt.length) : null
                    ].whereType<String>().join(' • '),
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),

            const SizedBox(height: 16),
            const Divider(),
            const SizedBox(height: 8),
            Align(
              alignment: Alignment.centerLeft,
              child: Text('Yorumlar', style: Theme.of(context).textTheme.titleMedium),
            ),
            const SizedBox(height: 8),
            
            Expanded(
              child: Builder(
                builder: (_) {
                  if (ui.loadingComments) {
                    return const Center(child: CircularProgressIndicator());
                  }
                  final comments = vm.comments;
                  if (comments.isEmpty) {
                    return const Center(child: Text('Henüz yorum yok'));
                  }
                  return ListView.separated(
                    itemCount: comments.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (_, i) {
                      final c = comments[i];
                      return Card(
                        elevation: 1,
                        child: InkWell(
                          onTap: () => _openVoteDialog(context, c),
                          borderRadius: BorderRadius.circular(12),
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(c.username, style: Theme.of(context).textTheme.labelLarge),
                                const SizedBox(height: 4),
                                Text(c.commentText),
                                const SizedBox(height: 8),
                                Text('👍${c.likes}   👎${c.dislikes}',
                                    style: Theme.of(context).textTheme.labelMedium),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  );
                },
              ),
            ),

            const SizedBox(height: 12),
            TextField(
              controller: _commentCtrl,
              decoration: const InputDecoration(
                labelText: 'Yorum yaz',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 8),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: ui.posting
                    ? null
                    : () async {
                  final text = _commentCtrl.text.trim();
                  if (text.isEmpty) return;

                  final userId = await _userId();
                  await context.read<EntryDetayViewModel>().addComment(
                    entryId: widget.entryId,
                    userId: userId,
                    text: text,
                  );
                  _commentCtrl.clear();
                },
                child: ui.posting
                    ? const SizedBox(
                  height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2),
                )
                    : const Text('Gönder'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _openVoteDialog(BuildContext context, Comment c) async {
    final userId = await _userId();

    if (!context.mounted) return;

    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Yorumu Oyla'),
        content: Text(c.commentText),
        actions: [
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              await context.read<EntryDetayViewModel>().voteComment(
                entryId: widget.entryId,
                commentId: c.id,
                userId: userId,
                isLike: false,
              );
            },
            child: const Text('👎 Beğenme'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              await context.read<EntryDetayViewModel>().voteComment(
                entryId: widget.entryId,
                commentId: c.id,
                userId: userId,
                isLike: true,
              );
            },
            child: const Text('👍 Beğen'),
          ),
        ],
      ),
    );
  }
}
