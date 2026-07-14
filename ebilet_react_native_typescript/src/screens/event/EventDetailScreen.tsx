import React, {useEffect, useState} from 'react';
import {
    ActivityIndicator,
    Alert,
    Image,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ApiClient} from '../../core/apiClient';
import {ApiService} from '../../core/apiService';
import {AppColors} from '../../core/appColors';
import {SessionManager} from '../../core/sessionManager';
import {AppButton} from '../../components/AppButton';
import {Event} from '../../models/Event';
import {RootStackParamList} from '../../navigation/routes';

type Props = NativeStackScreenProps<RootStackParamList, 'EventDetail'>;

export function EventDetailScreen({route, navigation}: Props) {
    const {eventId} = route.params;

    const [event, setEvent] = useState<Event | null>(null);

    const [loading, setLoading] = useState(false);
    const [buying, setBuying] = useState(false);

    const [statusMessage, setStatusMessage] = useState(
        'Etkinlik detayı yükleniyor...',
    );

    useEffect(() => {
        loadEventDetail();
    }, []);

    async function loadEventDetail() {
        try {
            setLoading(true);
            setStatusMessage('Etkinlik detayı yükleniyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getEventDetail({
                apiToken,
                eventId,
            });

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            if (!response.data) {
                setStatusMessage('Etkinlik bilgisi alınamadı.');
                return;
            }

            setEvent(response.data);
            setStatusMessage('Etkinlik detayı getirildi.');
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error
                    ? error.message
                    : 'Etkinlik detayı yüklenemedi.';

            setStatusMessage(message);
        }
    }

    async function buyTicket() {
        if (!event) {
            Alert.alert('Uyarı', 'Etkinlik bilgisi bulunamadı.');
            return;
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            Alert.alert('Uyarı', 'Bu etkinlik için kontenjan kalmamış.');
            return;
        }

        try {
            setBuying(true);
            setStatusMessage('Bilet oluşturuluyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.buyTicket({
                apiToken,
                eventId: event.id,
            });

            setBuying(false);

            if (!response.success) {
                setStatusMessage(response.message);
                Alert.alert('Uyarı', response.message);
                return;
            }

            const ticketCode =
                response.data?.ticketCode ?? response.data?.ticket_code ?? '-';

            setStatusMessage('Bilet başarıyla oluşturuldu.');
            Alert.alert(
                'Bilet Alındı',
                `Bilet kodu: ${ticketCode}`,
                [
                    {
                        text: 'Biletlerime Git',
                        onPress: () => {
                            navigation.replace('MyTickets');
                        },
                    },
                ],
                {
                    cancelable: false,
                },
            );
        } catch (error) {
            setBuying(false);

            const message =
                error instanceof Error ? error.message : 'Bilet oluşturulamadı.';

            setStatusMessage(message);
            Alert.alert('Bağlantı Hatası', message);
        }
    }

    function getPosterUrl(): string {
        const poster = event?.posterUrl ?? event?.poster_url ?? '';

        if (poster.length === 0) {
            return '';
        }

        if (poster.startsWith('http')) {
            return poster;
        }

        return ApiClient.baseUrl + poster;
    }

    function canBuy(): boolean {
        if (!event) {
            return false;
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        return remainingQuota > 0 && !buying && !loading;
    }

    function buyButtonText(): string {
        if (buying) {
            return 'Bilet Oluşturuluyor...';
        }

        if (!event) {
            return 'Bilet Al';
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            return 'Kontenjan Doldu';
        }

        return 'Bilet Al';
    }

    function buyButtonColor(): string {
        if (!event) {
            return AppColors.green;
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            return '#94A3B8';
        }

        return AppColors.green;
    }

    return (
        <View style={styles.root}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={
                    <RefreshControl refreshing={loading} onRefresh={loadEventDetail} />
                }>
                <View style={styles.statusRow}>
                    {loading || buying ? (
                        <ActivityIndicator size="small" color={AppColors.blue} />
                    ) : null}

                    <Text style={styles.statusText}>{statusMessage}</Text>
                </View>

                {loading && !event ? <LoadingCard /> : null}

                {!loading && !event ? <EmptyCard /> : null}

                {event ? (
                    <>
                        <PosterView posterUrl={getPosterUrl()} />

                        <View style={styles.infoCard}>
                            <Text style={styles.title}>{event.title}</Text>

                            <Text style={styles.description}>
                                {event.description ?? 'Açıklama bulunmuyor.'}
                            </Text>

                            <View style={styles.divider} />

                            <DetailLine
                                title="Tarih"
                                value={event.eventDate ?? event.event_date ?? '-'}
                            />

                            <DetailLine
                                title="Konum"
                                value={`${event.city?.name ?? event.cityName ?? event.city_name ?? '-'} / ${
                                    event.district?.name ??
                                    event.districtName ??
                                    event.district_name ??
                                    '-'
                                }`}
                            />

                            <DetailLine title="Sahne" value={event.venue?.name ?? '-'} />

                            <DetailLine title="Adres" value={event.venue?.address ?? '-'} />

                            <View style={styles.divider} />

                            <View style={styles.priceRow}>
                                <Text style={styles.priceText}>
                                    {event.basePrice ?? event.base_price ?? 0} TL
                                </Text>

                                <View style={styles.quotaBadge}>
                                    <Text style={styles.quotaText}>
                                        Kalan: {event.remainingQuota ?? event.remaining_quota ?? 0}
                                    </Text>
                                </View>
                            </View>

                            <AppButton
                                title={buyButtonText()}
                                loading={buying}
                                backgroundColor={buyButtonColor()}
                                onPress={canBuy() ? buyTicket : undefined}
                                style={styles.buyButton}
                            />
                        </View>
                    </>
                ) : null}
            </ScrollView>
        </View>
    );
}

type PosterProps = {
    posterUrl: string;
};

function PosterView({posterUrl}: PosterProps) {
    if (posterUrl.length === 0) {
        return (
            <View style={styles.posterPlaceholder}>
                <Text style={styles.posterIcon}>🖼️</Text>
            </View>
        );
    }

    return (
        <Image source={{uri: posterUrl}} style={styles.poster} resizeMode="cover" />
    );
}

type DetailLineProps = {
    title: string;
    value: string;
};

function DetailLine({title, value}: DetailLineProps) {
    return (
        <View style={styles.detailLine}>
            <Text style={styles.detailTitle}>{title}</Text>
            <Text style={styles.detailValue}>{value}</Text>
        </View>
    );
}

function LoadingCard() {
    return (
        <View style={styles.centerCard}>
            <ActivityIndicator size="large" color={AppColors.blue} />
            <Text style={styles.centerCardText}>Etkinlik detayı yükleniyor...</Text>
        </View>
    );
}

function EmptyCard() {
    return (
        <View style={styles.centerCard}>
            <Text style={styles.emptyIcon}>⚠️</Text>
            <Text style={styles.emptyTitle}>Etkinlik bilgisi bulunamadı.</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    root: {
        flex: 1,
        backgroundColor: AppColors.background,
    },
    scrollContent: {
        padding: 14,
        paddingBottom: 30,
    },
    statusRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        marginBottom: 12,
        paddingHorizontal: 4,
    },
    statusText: {
        flex: 1,
        fontSize: 14,
        color: AppColors.grayText,
    },
    centerCard: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 24,
        alignItems: 'center',
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 12,
        shadowOffset: {
            width: 0,
            height: 5,
        },
        elevation: 5,
    },
    centerCardText: {
        marginTop: 12,
        color: AppColors.grayText,
        fontSize: 14,
    },
    emptyIcon: {
        fontSize: 42,
        marginBottom: 10,
    },
    emptyTitle: {
        fontSize: 16,
        fontWeight: '800',
        color: AppColors.darkText,
        textAlign: 'center',
    },
    poster: {
        width: '100%',
        height: 250,
        borderRadius: 18,
        backgroundColor: '#E2E8F0',
        marginBottom: 14,
    },
    posterPlaceholder: {
        width: '100%',
        height: 250,
        borderRadius: 18,
        backgroundColor: '#E2E8F0',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 14,
    },
    posterIcon: {
        fontSize: 54,
    },
    infoCard: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 16,
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 12,
        shadowOffset: {
            width: 0,
            height: 5,
        },
        elevation: 5,
    },
    title: {
        fontSize: 25,
        fontWeight: '900',
        color: AppColors.darkText,
    },
    description: {
        marginTop: 10,
        fontSize: 15,
        color: AppColors.grayText,
        lineHeight: 22,
    },
    divider: {
        height: 1,
        backgroundColor: AppColors.border,
        marginVertical: 16,
    },
    detailLine: {
        marginBottom: 11,
    },
    detailTitle: {
        fontSize: 13,
        color: AppColors.grayText,
        marginBottom: 3,
    },
    detailValue: {
        fontSize: 15,
        color: AppColors.darkText,
        fontWeight: '800',
    },
    priceRow: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    priceText: {
        fontSize: 24,
        fontWeight: '900',
        color: AppColors.green,
    },
    quotaBadge: {
        marginLeft: 'auto',
        backgroundColor: '#DBEAFE',
        paddingHorizontal: 12,
        paddingVertical: 8,
        borderRadius: 12,
    },
    quotaText: {
        color: AppColors.blue,
        fontSize: 14,
        fontWeight: '800',
    },
    buyButton: {
        marginTop: 18,
    },
});