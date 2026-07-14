import React from 'react';
import {
    Image,
    Pressable,
    StyleSheet,
    Text,
    View,
} from 'react-native';

import {ApiClient} from '../core/apiClient';
import {AppColors} from '../core/appColors';
import {Ticket} from '../models/Ticket';

type Props = {
    ticket: Ticket;
    onPress: () => void;
};

export function TicketCard({ticket, onPress}: Props) {
    function getPosterUrl(): string {
        const poster = ticket.event?.posterUrl ?? ticket.event?.poster_url ?? '';

        if (poster.length === 0) {
            return '';
        }

        if (poster.startsWith('http')) {
            return poster;
        }

        return ApiClient.baseUrl + poster;
    }

    function getStatusText(): string {
        const status = ticket.status ?? ticket.ticketStatus ?? ticket.ticket_status ?? '-';

        if (status === 'active') {
            return 'Aktif';
        }

        if (status === 'used') {
            return 'Kullanıldı';
        }

        if (status === 'cancelled') {
            return 'İptal';
        }

        return status;
    }

    function getStatusColor(): string {
        const status = ticket.status ?? ticket.ticketStatus ?? ticket.ticket_status ?? '-';

        if (status === 'active') {
            return AppColors.green;
        }

        if (status === 'used') {
            return '#64748B';
        }

        if (status === 'cancelled') {
            return AppColors.red;
        }

        return AppColors.blue;
    }

    const posterUrl = getPosterUrl();

    const eventTitle =
        ticket.event?.title ?? ticket.eventTitle ?? ticket.event_title ?? 'Etkinlik bilgisi yok';

    const eventDate = ticket.event?.eventDate ?? ticket.event?.event_date ?? '-';

    const venueName =
        ticket.location?.venueName ??
        ticket.location?.venue_name ??
        ticket.venue?.name ??
        ticket.event?.venue?.name ??
        '-';

    const cityName =
        ticket.location?.cityName ??
        ticket.location?.city_name ??
        ticket.city?.name ??
        ticket.event?.city?.name ??
        '-';

    const districtName =
        ticket.location?.districtName ??
        ticket.location?.district_name ??
        ticket.district?.name ??
        ticket.event?.district?.name ??
        '-';

    const priceText = `${ticket.price ?? 0} TL`;

    const statusText = getStatusText();
    const statusColor = getStatusColor();

    return (
        <Pressable
            onPress={onPress}
            style={({pressed}) => [
                styles.card,
                {
                    opacity: pressed ? 0.85 : 1,
                },
            ]}>
            {posterUrl.length > 0 ? (
                <Image
                    source={{uri: posterUrl}}
                    style={styles.poster}
                    resizeMode="cover"
                />
            ) : (
                <View style={styles.posterPlaceholder}>
                    <Text style={styles.posterIcon}>🎟️</Text>
                </View>
            )}

            <View style={styles.content}>
                <Text style={styles.title} numberOfLines={2}>
                    {eventTitle}
                </Text>

                <Text style={styles.infoText} numberOfLines={1}>
                    Tarih: {eventDate}
                </Text>

                <Text style={styles.infoText} numberOfLines={1}>
                    Sahne: {venueName}
                </Text>

                <Text style={styles.smallText} numberOfLines={1}>
                    {cityName} / {districtName}
                </Text>

                <View style={styles.bottomRow}>
                    <Text style={styles.priceText}>{priceText}</Text>

                    <View
                        style={[
                            styles.statusBadge,
                            {
                                backgroundColor: `${statusColor}20`,
                            },
                        ]}>
                        <Text
                            style={[
                                styles.statusText,
                                {
                                    color: statusColor,
                                },
                            ]}>
                            {statusText}
                        </Text>
                    </View>
                </View>
            </View>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        marginBottom: 14,
        padding: 12,
        flexDirection: 'row',
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 12,
        shadowOffset: {
            width: 0,
            height: 5,
        },
        elevation: 5,
    },
    poster: {
        width: 95,
        height: 120,
        borderRadius: 12,
        backgroundColor: '#E2E8F0',
    },
    posterPlaceholder: {
        width: 95,
        height: 120,
        borderRadius: 12,
        backgroundColor: '#E2E8F0',
        alignItems: 'center',
        justifyContent: 'center',
    },
    posterIcon: {
        fontSize: 34,
    },
    content: {
        flex: 1,
        height: 120,
        marginLeft: 12,
    },
    title: {
        fontSize: 16,
        fontWeight: '800',
        color: AppColors.darkText,
        marginBottom: 5,
    },
    infoText: {
        fontSize: 12,
        color: AppColors.grayText,
        marginBottom: 4,
    },
    smallText: {
        fontSize: 12,
        color: AppColors.grayText,
    },
    bottomRow: {
        marginTop: 'auto',
        flexDirection: 'row',
        alignItems: 'center',
    },
    priceText: {
        fontSize: 16,
        fontWeight: '800',
        color: AppColors.green,
    },
    statusBadge: {
        marginLeft: 'auto',
        paddingHorizontal: 9,
        paddingVertical: 6,
        borderRadius: 10,
    },
    statusText: {
        fontSize: 12,
        fontWeight: '800',
    },
});