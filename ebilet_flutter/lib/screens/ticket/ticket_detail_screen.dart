import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../models/ticket_model.dart';

class TicketDetailScreen extends StatefulWidget {
  final int ticketId;

  const TicketDetailScreen({
    super.key,
    required this.ticketId,
  });

  @override
  State<TicketDetailScreen> createState() => _TicketDetailScreenState();
}

class _TicketDetailScreenState extends State<TicketDetailScreen> {
  TicketModel? _ticket;

  bool _isLoading = false;
  String _statusMessage = 'Bilet detayı yükleniyor...';

  @override
  void initState() {
    super.initState();
    _loadTicketDetail();
  }

  Future<void> _loadTicketDetail() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Bilet detayı yükleniyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getTicketDetail(
        apiToken: apiToken,
        ticketId: widget.ticketId,
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

      if (response.data == null) {
        setState(() {
          _statusMessage = 'Bilet bilgisi alınamadı.';
        });
        return;
      }

      setState(() {
        _ticket = response.data;
        _statusMessage = 'Bilet detayı getirildi.';
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

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  String _statusText(TicketModel ticket) {
    final status = ticket.status ?? ticket.ticketStatus ?? '-';

    if (status == 'active') {
      return 'Aktif Bilet';
    }

    if (status == 'used') {
      return 'Kullanıldı';
    }

    if (status == 'cancelled') {
      return 'İptal Edildi';
    }

    return status;
  }

  Color _statusColor(TicketModel ticket) {
    final status = ticket.status ?? ticket.ticketStatus ?? '-';

    if (status == 'active') {
      return AppColors.green;
    }

    if (status == 'used') {
      return Colors.grey;
    }

    if (status == 'cancelled') {
      return AppColors.red;
    }

    return AppColors.blue;
  }

  @override
  Widget build(BuildContext context) {
    final ticket = _ticket;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Bilet Detayı'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _loadTicketDetail,
          child: ListView(
            padding: const EdgeInsets.all(14),
            children: [
              _buildStatusRow(),
              const SizedBox(height: 12),
              if (_isLoading && ticket == null) _buildLoadingCard(),
              if (!_isLoading && ticket == null) _buildEmptyCard(),
              if (ticket != null) _buildTicketContent(ticket),
            ],
          ),
        ),
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
            'Bilet detayı yükleniyor...',
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
            Icons.error_outline,
            size: 48,
            color: AppColors.grayText,
          ),
          SizedBox(height: 10),
          Text(
            'Bilet bilgisi bulunamadı.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 16,
              color: AppColors.darkText,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTicketContent(TicketModel ticket) {
    final eventTitle = ticket.event?.title ?? ticket.eventTitle ?? 'Etkinlik';

    final venueName =
        ticket.venue?.name ??
            ticket.location?.venueName ??
            ticket.event?.venue?.name ??
            '-';

    final cityName =
        ticket.city?.name ??
            ticket.location?.cityName ??
            ticket.event?.city?.name ??
            '-';

    final districtName =
        ticket.district?.name ??
            ticket.location?.districtName ??
            ticket.event?.district?.name ??
            '-';

    final qrText = ticket.qrCodeText ?? ticket.ticketCode ?? '';
    final ticketCodeText = ticket.ticketCode ?? qrText;
    final priceText = '${ticket.price?.toInt() ?? 0} TL';
    final usedAt = ticket.usedAt;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: _cardDecoration(),
      child: Column(
        children: [
          Text(
            eventTitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 23,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 12),
          _buildStatusBadge(ticket),
          const SizedBox(height: 18),
          _buildQrArea(qrText),
          const SizedBox(height: 12),
          Text(
            ticketCodeText,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 18),
          const Divider(),
          const SizedBox(height: 10),
          _buildDetailLine(
            title: 'Tarih',
            value: ticket.event?.eventDate ?? '-',
          ),
          _buildDetailLine(
            title: 'Sahne',
            value: venueName,
          ),
          _buildDetailLine(
            title: 'Konum',
            value: '$cityName / $districtName',
          ),
          _buildDetailLine(
            title: 'Fiyat',
            value: priceText,
            valueColor: AppColors.green,
          ),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerLeft,
            child: Text(
              usedAt != null && usedAt.isNotEmpty
                  ? 'Kullanım zamanı: $usedAt'
                  : 'Bilet henüz kullanılmadı.',
              style: const TextStyle(
                fontSize: 14,
                color: AppColors.grayText,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBadge(TicketModel ticket) {
    final text = _statusText(ticket);
    final color = _statusColor(ticket);

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 14,
        vertical: 8,
      ),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: color,
        ),
      ),
    );
  }

  Widget _buildQrArea(String qrText) {
    if (qrText.isEmpty) {
      return Container(
        width: 260,
        height: 260,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Colors.grey.shade300,
          ),
        ),
        child: const Center(
          child: Text(
            'QR oluşturulamadı',
            style: TextStyle(
              color: AppColors.grayText,
            ),
          ),
        ),
      );
    }

    return Container(
      width: 260,
      height: 260,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: Colors.grey.shade300,
        ),
      ),
      child: QrImageView(
        data: qrText,
        version: QrVersions.auto,
        gapless: true,
        backgroundColor: Colors.white,
      ),
    );
  }

  Widget _buildDetailLine({
    required String title,
    required String value,
    Color valueColor = AppColors.darkText,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 11),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Align(
            alignment: Alignment.centerLeft,
            child: Text(
              title,
              style: const TextStyle(
                fontSize: 13,
                color: AppColors.grayText,
              ),
            ),
          ),
          const SizedBox(height: 3),
          Align(
            alignment: Alignment.centerLeft,
            child: Text(
              value,
              style: TextStyle(
                fontSize: 15,
                color: valueColor,
                fontWeight: FontWeight.bold,
              ),
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