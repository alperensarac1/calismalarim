import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../core/app_colors.dart';
import '../models/event_model.dart';

class EventCard extends StatelessWidget {
  final EventModel event;
  final VoidCallback onTap;

  const EventCard({
    super.key,
    required this.event,
    required this.onTap,
  });

  String _posterUrl() {
    final poster = event.posterUrl ?? '';

    if (poster.isEmpty) {
      return '';
    }

    if (poster.startsWith('http')) {
      return poster;
    }

    return ApiClient.baseUrl + poster;
  }

  @override
  Widget build(BuildContext context) {
    final posterUrl = _posterUrl();

    final venueName = event.venue?.name ?? '-';
    final cityName = event.cityName ?? event.city?.name ?? '-';
    final districtName = event.districtName ?? event.district?.name ?? '-';
    final priceText = '${event.basePrice?.toInt() ?? 0} TL';
    final quotaText = 'Kalan: ${event.remainingQuota ?? 0}';

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(18),
      child: Container(
        margin: const EdgeInsets.only(bottom: 14),
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
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(18),
              ),
              child: SizedBox(
                width: double.infinity,
                height: 190,
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
            ),
            Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    event.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 19,
                      fontWeight: FontWeight.bold,
                      color: AppColors.darkText,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Tarih: ${event.eventDate ?? '-'}',
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.grayText,
                    ),
                  ),
                  const SizedBox(height: 5),
                  Text(
                    'Sahne: $venueName',
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.grayText,
                    ),
                  ),
                  const SizedBox(height: 5),
                  Text(
                    '$cityName / $districtName',
                    style: const TextStyle(
                      fontSize: 13,
                      color: AppColors.grayText,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Text(
                        priceText,
                        style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: AppColors.green,
                        ),
                      ),
                      const Spacer(),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 10,
                          vertical: 7,
                        ),
                        decoration: BoxDecoration(
                          color: AppColors.blue.withOpacity(0.10),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Text(
                          quotaText,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.blue,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
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
          Icons.image_outlined,
          size: 52,
          color: AppColors.grayText,
        ),
      ),
    );
  }
}