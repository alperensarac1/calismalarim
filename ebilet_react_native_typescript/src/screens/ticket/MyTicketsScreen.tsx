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
import {NativeStackScreenProps} from '@react-navigation/native-stack';

import {ApiService} from '../../core/apiService';
import {AppColors} from '../../core/appColors';
import {SessionManager} from '../../core/sessionManager';
import {Ticket, getResolvedTicketId} from '../../models/Ticket';
import {TicketCard} from '../../components/TicketCard';
import {RootStackParamList} from '../../navigation/routes';

type Props = NativeStackScreenProps<RootStackParamList, 'MyTickets'>;

export function MyTicketsScreen({navigation}: Props) {
    const [tickets, setTickets] = useState<Ticket[]>([]);

    const [loading, setLoading] = useState(false);
    const [statusMessage, setStatusMessage] = useState('Biletler yükleniyor...');

    useEffect(() => {
        loadMyTickets();
    }, []);

    async function loadMyTickets() {
        try {
            setLoading(true);
            setStatusMessage('Biletler yükleniyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getMyTickets(apiToken);

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                Alert.alert('Uyarı', response.message);
                return;
            }

            const list = response.data ?? [];

            setTickets(list);

            if (list.length === 0) {
                setStatusMessage('Henüz satın alınmış biletin yok.');
            } else {
                setStatusMessage(`${list.length} bilet listelendi.`);
            }
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : 'Biletler yüklenemedi.';

            setStatusMessage(message);
            Alert.alert('Hata', message);
        }
    }

    function openTicketDetail(ticket: Ticket) {
        const ticketId = getResolvedTicketId(ticket);

        if (ticketId <= 0) {
            Alert.alert('Uyarı', 'Bilet ID alınamadı.');
            return;
        }

        navigation.navigate('TicketDetail', {
            ticketId,
        });
    }

    return (
        <View style={styles.root}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={
                    <RefreshControl refreshing={loading} onRefresh={loadMyTickets} />
                }>
                <View style={styles.headerCard}>
                    <Text style={styles.headerTitle}>Biletlerim</Text>

                    <Text style={styles.headerDescription}>
                        Satın aldığın biletleri ve QR kodlarını buradan görüntüleyebilirsin.
                    </Text>
                </View>

                <View style={styles.statusRow}>
                    {loading ? (
                        <ActivityIndicator size="small" color={AppColors.blue} />
                    ) : null}

                    <Text style={styles.statusText}>{statusMessage}</Text>
                </View>

                {loading && tickets.length === 0 ? <LoadingCard /> : null}

                {!loading && tickets.length === 0 ? <EmptyTicketsCard /> : null}

                {tickets.map(ticket => {
                    const ticketId = getResolvedTicketId(ticket);

                    return (
                        <TicketCard
                            key={ticketId}
                            ticket={ticket}
                            onPress={() => openTicketDetail(ticket)}
                        />
                    );
                })}
            </ScrollView>
        </View>
    );
}

function LoadingCard() {
    return (
        <View style={styles.centerCard}>
            <ActivityIndicator size="large" color={AppColors.blue} />
            <Text style={styles.centerCardText}>Biletler yükleniyor...</Text>
        </View>
    );
}

function EmptyTicketsCard() {
    return (
        <View style={styles.centerCard}>
            <Text style={styles.emptyIcon}>🎟️</Text>

            <Text style={styles.emptyTitle}>Henüz biletin yok</Text>

            <Text style={styles.emptyDescription}>
                Bir etkinlik seçip bilet satın aldığında burada görünecek.
            </Text>
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
    headerCard: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 16,
        marginBottom: 12,
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 12,
        shadowOffset: {
            width: 0,
            height: 5,
        },
        elevation: 5,
    },
    headerTitle: {
        fontSize: 26,
        fontWeight: '900',
        color: AppColors.darkText,
    },
    headerDescription: {
        marginTop: 8,
        fontSize: 14,
        color: AppColors.grayText,
        lineHeight: 20,
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
        fontSize: 46,
        marginBottom: 12,
    },
    emptyTitle: {
        fontSize: 18,
        fontWeight: '900',
        color: AppColors.darkText,
        textAlign: 'center',
    },
    emptyDescription: {
        marginTop: 7,
        fontSize: 14,
        color: AppColors.grayText,
        textAlign: 'center',
        lineHeight: 20,
    },
});