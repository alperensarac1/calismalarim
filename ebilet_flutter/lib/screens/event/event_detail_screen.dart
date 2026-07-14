import 'package:flutter/material.dart';

import '../../core/api_client.dart';
import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../models/event_model.dart';
import '../../widgets/app_button.dart';

class EventDetailScreen extends StatefulWidget {
  final int eventId;

  const EventDetailScreen({
    super.key,
    required this.eventId,
  });

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  EventModel? _event;

  bool _isLoading = false;
  bool _isBuying = false;

  String _statusMessage = 'Etkinlik detayı yükleniyor...';

  @override
  void initState() {
    super.initState();
    _loadEventDetail();
  }

  Future<void> _loadEventDetail() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Etkinlik detayı yükleniyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getEventDetail(
        apiToken: apiToken,
        eventId: widget.eventId,
      );

      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });
        return;
      }

      if (response.data == null) {
        setState(() {
          _statusMessage = 'Etkinlik bilgisi alınamadı.';
        });
        return;
      }

      setState(() {
        _event = response.data;
        _statusMessage = 'Etkinlik detayı getirildi.';
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
        _statusMessage = e.toString();
      });
    }
  }

  Future<void> _buyTicket() async {
    final event = _event;

    if (event == null) {
      _showMessage('Etkinlik bilgisi bulunamadı.');
      return;
    }

    final remainingQuota = event.remainingQuota ?? 0;

    if (remainingQuota <= 0) {
      _showMessage('Bu etkinlik için kontenjan kalmamış.');
      return;
    }

    setState(() {
      _isBuying = true;
      _statusMessage = 'Bilet oluşturuluyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.buyTicket(
        apiToken: apiToken,
        eventId: event.id,
      );

      if (!mounted) return;

      setState(() {
        _isBuying = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });

        _showMessage(response.message);
        return;
      }

      final ticketCode = response.data?.ticketCode ?? '-';

      setState(() {
        _statusMessage = 'Bilet başarıyla oluşturuldu.';
      });

      _showTicketSuccessDialog(ticketCode);
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isBuying = false;
        _statusMessage = e.toString();
      });

      _showMessage(e.toString());
    }
  }

  void _showTicketSuccessDialog(String ticketCode) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Bilet Alındı'),
          content: Text('Bilet kodu: $ticketCode'),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(dialogContext);

                /*
                  Bir sonraki adımda MyTicketsScreen yapınca
                  burada direkt Biletlerim ekranına yönlendireceğiz.
                */
                Navigator.pop(context);
              },
              child: const Text('Tamam'),
            ),
          ],
        );
      },
    );
  }

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  String _posterUrl(EventModel event) {
    final poster = event.posterUrl ?? '';

    if (poster.isEmpty) {
      return '';
    }

    if (poster.startsWith('http')) {
      return poster;
    }

    return ApiClient.baseUrl + poster;
  }

  bool _canBuy(EventModel event) {
    final remainingQuota = event.remainingQuota ?? 0;
    return remainingQuota > 0 && !_isBuying && !_isLoading;
  }

  String _buyButtonText(EventModel event) {
    if (_isBuying) {
      return 'Bilet Oluşturuluyor...';
    }

    final remainingQuota = event.remainingQuota ?? 0;

    if (remainingQuota <= 0) {
      return 'Kontenjan Doldu';
    }

    return 'Bilet Al';
  }

  Color _buyButtonColor(EventModel event) {
    final remainingQuota = event.remainingQuota ?? 0;

    if (remainingQuota <= 0) {
      return Colors.grey;
    }

    return AppColors.green;
  }

  @override
  Widget build(BuildContext context) {
    final event = _event;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Etkinlik Detayı'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _loadEventDetail,
          child: ListView(
            padding: const EdgeInsets.all(14),
            children: [
              _buildStatusRow(),
              const SizedBox(height: 12),
              if (_isLoading && event == null) _buildLoadingCard(),
              if (!_isLoading && event == null) _buildEmptyCard(),
              if (event != null) _buildContent(event),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusRow() {
    return Row(
      children: [
        if (_isLoading || _isBuying) ...[
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
            'Etkinlik detayı yükleniyor...',
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
            'Etkinlik bilgisi bulunamadı.',
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

  Widget _buildContent(EventModel event) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildPoster(event),
        const SizedBox(height: 14),
        _buildInfoCard(event),
      ],
    );
  }

  Widget _buildPoster(EventModel event) {
    final posterUrl = _posterUrl(event);

    return ClipRRect(
      borderRadius: BorderRadius.circular(18),
      child: SizedBox(
        width: double.infinity,
        height: 250,
        child: posterUrl.isEmpty
            ? _buildPosterPlaceholder()
            : Image.network(
          posterUrl,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            return _buildPosterPlaceholder();
          },
          loadingBuilder: (context, child, loadingProgress) {
            if (loadingProgress == null) {
              return child;
            }

            return const Center(
              child: CircularProgressIndicator(),
            );
          },
        ),
      ),
    );
  }

  Widget _buildPosterPlaceholder() {
    return Container(
      color: const Color(0xFFE2E8F0),
      child: const Center(
        child: Icon(
          Icons.image_outlined,
          size: 56,
          color: AppColors.grayText,
        ),
      ),
    );
  }

  Widget _buildInfoCard(EventModel event) {
    final cityName = event.city?.name ?? event.cityName ?? '-';
    final districtName = event.district?.name ?? event.districtName ?? '-';
    final venueName = event.venue?.name ?? '-';
    final venueAddress = event.venue?.address ?? '-';
    final priceText = '${event.basePrice?.toInt() ?? 0} TL';
    final quotaText = 'Kalan: ${event.remainingQuota ?? 0}';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            event.title,
            style: const TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            event.description ?? 'Açıklama bulunmuyor.',
            style: const TextStyle(
              fontSize: 15,
              color: AppColors.grayText,
              height: 1.35,
            ),
          ),
          const SizedBox(height: 16),
          const Divider(),
          const SizedBox(height: 8),
          _buildDetailLine(
            title: 'Tarih',
            value: event.eventDate ?? '-',
          ),
          _buildDetailLine(
            title: 'Konum',
            value: '$cityName / $districtName',
          ),
          _buildDetailLine(
            title: 'Sahne',
            value: venueName,
          ),
          _buildDetailLine(
            title: 'Adres',
            value: venueAddress,
          ),
          const SizedBox(height: 12),
          const Divider(),
          const SizedBox(height: 12),
          Row(
            children: [
              Text(
                priceText,
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: AppColors.green,
                ),
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: AppColors.blue.withOpacity(0.10),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  quotaText,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: AppColors.blue,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 18),
          AppButton(
            text: _buyButtonText(event),
            backgroundColor: _buyButtonColor(event),
            isLoading: _isBuying,
            onPressed: _canBuy(event) ? _buyTicket : null,
          ),
        ],
      ),
    );
  }

  Widget _buildDetailLine({
    required String title,
    required String value,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 11),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 3),
          Text(
            value,
            style: const TextStyle(
              fontSize: 15,
              color: AppColors.darkText,
              fontWeight: FontWeight.bold,
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