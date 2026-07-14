import 'package:flutter/material.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../models/ticket_model.dart';
import '../../widgets/ticket_card.dart';
import 'ticket_detail_screen.dart';

class MyTicketsScreen extends StatefulWidget {
  const MyTicketsScreen({super.key});

  @override
  State<MyTicketsScreen> createState() => _MyTicketsScreenState();
}

class _MyTicketsScreenState extends State<MyTicketsScreen> {
  List<TicketModel> _tickets = [];

  bool _isLoading = false;
  String _statusMessage = 'Biletler yükleniyor...';

  @override
  void initState() {
    super.initState();
    _loadMyTickets();
  }

  Future<void> _loadMyTickets() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Biletler yükleniyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getMyTickets(
        apiToken: apiToken,
      );

      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });

        _showMessage(response.message);
        return;
      }

      setState(() {
        _tickets = response.data ?? [];

        if (_tickets.isEmpty) {
          _statusMessage = 'Henüz satın alınmış biletin yok.';
        } else {
          _statusMessage = '${_tickets.length} bilet listelendi.';
        }
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
        _statusMessage = e.toString();
      });

      _showMessage(e.toString());
    }
  }

  void _openTicketDetail(TicketModel ticket) {
    final ticketId = ticket.resolvedTicketId;

    if (ticketId <= 0) {
      _showMessage('Bilet ID alınamadı.');
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => TicketDetailScreen(
          ticketId: ticketId,
        ),
      ),
    ).then((_) {
      _loadMyTickets();
    });
  }

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  bool get _showEmpty {
    return !_isLoading && _tickets.isEmpty;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Biletlerim'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _loadMyTickets,
          child: ListView(
            padding: const EdgeInsets.all(14),
            children: [
              _buildHeaderCard(),
              const SizedBox(height: 12),
              _buildStatusRow(),
              const SizedBox(height: 12),
              if (_isLoading && _tickets.isEmpty) _buildLoadingCard(),
              if (_showEmpty) _buildEmptyCard(),
              ..._tickets.map(
                    (ticket) => TicketCard(
                  ticket: ticket,
                  onTap: () => _openTicketDetail(ticket),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeaderCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Biletlerim',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Satın aldığın biletleri ve QR kodlarını buradan görüntüleyebilirsin.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusRow() {
    return Row(
      children: [
        if (_isLoading) ...[
          const SizedBox(
            width: 18,
            height: 18,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
          const SizedBox(width: 8),
        ],
        Expanded(
          child: Text(
            _statusMessage,
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildLoadingCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: _cardDecoration(),
      child: const Column(
        children: [
          CircularProgressIndicator(),
          SizedBox(height: 12),
          Text(
            'Biletler yükleniyor...',
            style: TextStyle(
              color: AppColors.grayText,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: _cardDecoration(),
      child: const Column(
        children: [
          Icon(
            Icons.confirmation_number_outlined,
            size: 50,
            color: AppColors.grayText,
          ),
          SizedBox(height: 12),
          Text(
            'Henüz biletin yok',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          SizedBox(height: 7),
          Text(
            'Bir etkinlik seçip bilet satın aldığında burada görünecek.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
        ],
      ),
    );
  }

  BoxDecoration _cardDecoration() {
    return BoxDecoration(
      color: AppColors.cardBackground,
      borderRadius: BorderRadius.circular(18),
      boxShadow: [
        BoxShadow(
          color: Colors.black.withOpacity(0.08),
          blurRadius: 12,
          offset: const Offset(0, 5),
        ),
      ],
    );
  }
}