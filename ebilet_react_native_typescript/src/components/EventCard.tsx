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
import {Event} from '../models/Event';

type Props = {
    event: Event;
    onPress: () => void;
};

export function EventCard({event, onPress}: Props) {
    function getPosterUrl(): string {
        const poster = event.posterUrl ?? event.poster_url ?? '';

        if (poster.length === 0) {
            return '';
        }

        if (poster.startsWith('http')) {
            return poster;
        }

        return ApiClient.baseUrl + poster;
    }

    const posterUrl = getPosterUrl();

    const venueName = event.venue?.name ?? '-';
    const cityName = event.cityName ?? event.city_name ?? event.city?.name ?? '-';
    const districtName =
        event.districtName ?? event.district_name ?? event.district?.name ?? '-';

    const priceText = `${event.basePrice ?? event.base_price ?? 0} TL`;
    const quotaText = `Kalan: ${event.remainingQuota ?? event.remaining_quota ?? 0}`;

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
                    <Text style={styles.posterIcon}>🖼️</Text>
                </View>
            )}

            <View style={styles.content}>
                <Text style={styles.title} numberOfLines={2}>
                    {event.title}
                </Text>

                <Text style={styles.infoText} numberOfLines={1}>
                    Tarih: {event.eventDate ?? event.event_date ?? '-'}
                </Text>

                <Text style={styles.infoText} numberOfLines={1}>
                    Sahne: {venueName}
                </Text>

                <Text style={styles.smallText} numberOfLines={1}>
                    {cityName} / {districtName}
                </Text>

                <View style={styles.bottomRow}>
                    <Text style={styles.priceText}>{priceText}</Text>

                    <View style={styles.quotaBadge}>
                        <Text style={styles.quotaText}>{quotaText}</Text>
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
        overflow: 'hidden',
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
        width: '100%',
        height: 190,
        backgroundColor: '#E2E8F0',
    },
    posterPlaceholder: {
        width: '100%',
        height: 190,
        backgroundColor: '#E2E8F0',
        alignItems: 'center',
        justifyContent: 'center',
    },
    posterIcon: {
        fontSize: 46,
    },
    content: {
        padding: 14,
    },
    title: {
        fontSize: 19,
        fontWeight: '800',
        color: AppColors.darkText,
        marginBottom: 8,
    },
    infoText: {
        fontSize: 14,
        color: AppColors.grayText,
        marginBottom: 5,
    },
    smallText: {
        fontSize: 13,
        color: AppColors.grayText,
        marginBottom: 12,
    },
    bottomRow: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    priceText: {
        fontSize: 20,
        fontWeight: '800',
        color: AppColors.green,
    },
    quotaBadge: {
        marginLeft: 'auto',
        backgroundColor: '#DBEAFE',
        paddingHorizontal: 10,
        paddingVertical: 7,
        borderRadius: 10,
    },
    quotaText: {
        color: AppColors.blue,
        fontSize: 13,
        fontWeight: '800',
    },
});