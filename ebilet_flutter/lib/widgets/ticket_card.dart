import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../core/app_colors.dart';
import '../models/ticket_model.dart';

class TicketCard extends StatelessWidget {
  final TicketModel ticket;
  final VoidCallback onTap;

  const TicketCard({
    super.key,
    required this.ticket,
    required this.onTap,
  });

  String _posterUrl() {
    final poster = ticket.event?.posterUrl ?? '';

    if (poster.isEmpty) {
      return '';
    }

    if (poster.startsWith('http')) {
      return poster;
    }

    return ApiClient.baseUrl + poster;
  }

  String _statusText() {
    final status = ticket.status ?? ticket.ticketStatus ?? '-';

    if (status == 'active') {
      return 'Aktif';
    }

    if (status == 'used') {
      return 'Kullanıldı';
    }

    if (status == 'cancelled') {
      return 'İptal';
    }

    return status;
  }

  Color _statusColor() {
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
    final posterUrl = _posterUrl();

    final eventTitle = ticket.event?.title ?? ticket.eventTitle ?? 'Etkinlik bilgisi yok';
    final eventDate = ticket.event?.eventDate ?? '-';

    final venueName =
        ticket.location?.venueName ??
            ticket.venue?.name ??
            ticket.event?.venue?.name ??
            '-';

    final cityName =
        ticket.location?.cityName ??
            ticket.city?.name ??
            ticket.event?.city?.name ??
            '-';

    final districtName =
        ticket.location?.districtName ??
            ticket.district?.name ??
            ticket.event?.district?.name ??
            '-';

    final priceText = '${ticket.price?.toInt() ?? 0} TL';
    final statusText = _statusText();
    final statusColor = _statusColor();

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(18),
      child: Container(
        margin: const EdgeInsets.only(bottom: 14),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.08),
              blurRadius: 12,
              offset: const Offset(0, 5),
            ),
          ],
        ),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: SizedBox(
                width: 95,
                height: 120,
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
                      child: CircularProgressIndicator(strokeWidth: 2),
                    );
                  },
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: SizedBox(
                height: 120,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      eventTitle,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: AppColors.darkText,
                      ),
                    ),
                    const SizedBox(height: 5),
                    Text(
                      'Tarih: $eventDate',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.grayText,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Sahne: $venueName',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.grayText,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '$cityName / $districtName',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.grayText,
                      ),
                    ),
                    const Spacer(),
                    Row(
                      children: [
                        Text(
                          priceText,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: AppColors.green,
                          ),
                        ),
                        const Spacer(),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 9,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: statusColor.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            statusText,
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: statusColor,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPosterPlaceholder() {
    return Container(
      color: const Color(0xFFE2E8F0),
      child: const Center(
        child: Icon(
          Icons.confirmation_number_outlined,
          size: 38,
          color: AppColors.grayText,
        ),
      ),
    );
  }
}