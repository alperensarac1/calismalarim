import React, {useEffect, useState} from 'react';
import {
    ActivityIndicator,
    Alert,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ApiService} from '../../core/apiService';
import {AppColors} from '../../core/appColors';
import {SessionManager} from '../../core/sessionManager';
import {Ticket} from '../../models/Ticket';
import {RootStackParamList} from '../../navigation/routes';

type Props = NativeStackScreenProps<RootStackParamList, 'TicketDetail'>;

export function TicketDetailScreen({route}: Props) {
    const {ticketId} = route.params;

    const [ticket, setTicket] = useState<Ticket | null>(null);

    const [loading, setLoading] = useState(false);
    const [statusMessage, setStatusMessage] = useState(
        'Bilet detayı yükleniyor...',
    );

    useEffect(() => {
        loadTicketDetail();
    }, []);

    async function loadTicketDetail() {
        try {
            setLoading(true);
            setStatusMessage('Bilet detayı yükleniyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getTicketDetail({
                apiToken,
                ticketId,
            });

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                Alert.alert('Uyarı', response.message);
                return;
            }

            if (!response.data) {
                setStatusMessage('Bilet bilgisi alınamadı.');
                return;
            }

            setTicket(response.data);
            setStatusMessage('Bilet detayı getirildi.');
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : 'Bilet detayı yüklenemedi.';

            setStatusMessage(message);
            Alert.alert('Hata', message);
        }
    }

    function getStatusText(): string {
        const status = ticket?.status ?? ticket?.ticketStatus ?? ticket?.ticket_status ?? '-';

        if (status === 'active') {
            return 'Aktif Bilet';
        }

        if (status === 'used') {
            return 'Kullanıldı';
        }

        if (status === 'cancelled') {
            return 'İptal Edildi';
        }

        return status;
    }

    function getStatusColor(): string {
        const status = ticket?.status ?? ticket?.ticketStatus ?? ticket?.ticket_status ?? '-';

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

    return (
        <View style={styles.root}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={
                    <RefreshControl refreshing={loading} onRefresh={loadTicketDetail} />
                }>
                <View style={styles.statusRow}>
                    {loading ? (
                        <ActivityIndicator size="small" color={AppColors.blue} />
                    ) : null}

                    <Text style={styles.statusText}>{statusMessage}</Text>
                </View>

                {loading && !ticket ? <LoadingCard /> : null}

                {!loading && !ticket ? <EmptyCard /> : null}

                {ticket ? (
                    <TicketContent
                        ticket={ticket}
                        statusText={getStatusText()}
                        statusColor={getStatusColor()}
                    />
                ) : null}
            </ScrollView>
        </View>
    );
}

type TicketContentProps = {
    ticket: Ticket;
    statusText: string;
    statusColor: string;
};

function TicketContent({ticket, statusText, statusColor}: TicketContentProps) {
    const eventTitle =
        ticket.event?.title ?? ticket.eventTitle ?? ticket.event_title ?? 'Etkinlik';

    const eventDate = ticket.event?.eventDate ?? ticket.event?.event_date ?? '-';

    const venueName =
        ticket.venue?.name ??
        ticket.location?.venueName ??
        ticket.location?.venue_name ??
        ticket.event?.venue?.name ??
        '-';

    const cityName =
        ticket.city?.name ??
        ticket.location?.cityName ??
        ticket.location?.city_name ??
        ticket.event?.city?.name ??
        '-';

    const districtName =
        ticket.district?.name ??
        ticket.location?.districtName ??
        ticket.location?.district_name ??
        ticket.event?.district?.name ??
        '-';

    const qrText = ticket.qrCodeText ?? ticket.qr_code_text ?? ticket.ticketCode ?? ticket.ticket_code ?? '';

    const ticketCodeText = ticket.ticketCode ?? ticket.ticket_code ?? qrText;

    const priceText = `${ticket.price ?? 0} TL`;

    const usedAt = ticket.usedAt ?? ticket.used_at ?? '';

    return (
        <View style={styles.ticketCard}>
            <Text style={styles.eventTitle}>{eventTitle}</Text>

            <View
                style={[
                    styles.statusBadge,
                    {
                        backgroundColor: `${statusColor}20`,
                    },
                ]}>
                <Text
                    style={[
                        styles.statusBadgeText,
                        {
                            color: statusColor,
                        },
                    ]}>
                    {statusText}
                </Text>
            </View>

            <View style={styles.qrBox}>
                {qrText.length > 0 ? (
                    <QRCode value={qrText} size={230} backgroundColor="#FFFFFF" />
                ) : (
                    <Text style={styles.qrErrorText}>QR oluşturulamadı</Text>
                )}
            </View>

            <Text style={styles.ticketCodeText}>{ticketCodeText}</Text>

            <View style={styles.divider} />

            <DetailLine title="Tarih" value={eventDate} />
            <DetailLine title="Sahne" value={venueName} />
            <DetailLine title="Konum" value={`${cityName} / ${districtName}`} />
            <DetailLine title="Fiyat" value={priceText} valueColor={AppColors.green} />

            <Text style={styles.usedText}>
                {usedAt.length > 0
                    ? `Kullanım zamanı: ${usedAt}`
                    : 'Bilet henüz kullanılmadı.'}
            </Text>
        </View>
    );
}

type DetailLineProps = {
    title: string;
    value: string;
    valueColor?: string;
};

function DetailLine({title, value, valueColor = AppColors.darkText}: DetailLineProps) {
    return (
        <View style={styles.detailLine}>
            <Text style={styles.detailTitle}>{title}</Text>
            <Text style={[styles.detailValue, {color: valueColor}]}>{value}</Text>
        </View>
    );
}

function LoadingCard() {
    return (
        <View style={styles.centerCard}>
            <ActivityIndicator size="large" color={AppColors.blue} />
            <Text style={styles.centerCardText}>Bilet detayı yükleniyor...</Text>
        </View>
    );
}

function EmptyCard() {
    return (
        <View style={styles.centerCard}>
            <Text style={styles.emptyIcon}>⚠️</Text>
            <Text style={styles.emptyTitle}>Bilet bilgisi bulunamadı.</Text>
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
    ticketCard: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 18,
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
    eventTitle: {
        fontSize: 23,
        fontWeight: '900',
        color: AppColors.darkText,
        textAlign: 'center',
    },
    statusBadge: {
        marginTop: 12,
        paddingHorizontal: 14,
        paddingVertical: 8,
        borderRadius: 12,
    },
    statusBadgeText: {
        fontSize: 14,
        fontWeight: '900',
    },
    qrBox: {
        width: 260,
        height: 260,
        marginTop: 18,
        borderRadius: 16,
        backgroundColor: '#FFFFFF',
        borderWidth: 1,
        borderColor: AppColors.border,
        alignItems: 'center',
        justifyContent: 'center',
    },
    qrErrorText: {
        fontSize: 14,
        color: AppColors.grayText,
    },
    ticketCodeText: {
        marginTop: 12,
        fontSize: 14,
        color: AppColors.grayText,
        textAlign: 'center',
    },
    divider: {
        width: '100%',
        height: 1,
        backgroundColor: AppColors.border,
        marginVertical: 18,
    },
    detailLine: {
        width: '100%',
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
    usedText: {
        width: '100%',
        marginTop: 8,
        fontSize: 14,
        color: AppColors.grayText,
    },
});