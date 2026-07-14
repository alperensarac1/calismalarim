import React, {useEffect, useState} from 'react';
import {
    ActivityIndicator,
    Alert,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from 'react-native';
import {
    Camera,
    useCameraDevice,
    useCodeScanner,
} from 'react-native-vision-camera';

import {ApiService} from '../../core/apiService';
import {AppColors} from '../../core/appColors';
import {SessionManager} from '../../core/sessionManager';
import {AppButton} from '../../components/AppButton';
import {AppTextField} from '../../components/AppTextField';
import {Ticket} from '../../models/Ticket';

type ScannerResultType = 'neutral' | 'success' | 'failed';

export function TicketScannerScreen() {
    const device = useCameraDevice('back');

    const [manualCode, setManualCode] = useState('');

    const [hasPermission, setHasPermission] = useState(false);
    const [scannerActive, setScannerActive] = useState(false);

    const [checking, setChecking] = useState(false);

    const [fullName, setFullName] = useState('');
    const [role, setRole] = useState('user');
    const [isStaffOrAdmin, setIsStaffOrAdmin] = useState(false);

    const [statusMessage, setStatusMessage] = useState(
        'QR kod okutabilir veya manuel bilet kodu girebilirsin.',
    );

    const [resultType, setResultType] = useState<ScannerResultType>('neutral');
    const [resultTitle, setResultTitle] = useState('Henüz kontrol yapılmadı');
    const [resultMessage, setResultMessage] = useState(
        'QR okutulduğunda veya manuel kod girildiğinde sonuç burada görünecek.',
    );

    const [checkedTicket, setCheckedTicket] = useState<Ticket | null>(null);

    const codeScanner = useCodeScanner({
        codeTypes: ['qr'],
        onCodeScanned: codes => {
            if (checking || !scannerActive) {
                return;
            }

            const firstCode = codes[0];

            const value = firstCode?.value?.trim();

            if (!value) {
                return;
            }

            setManualCode(value);
            setScannerActive(false);

            checkTicket(value);
        },
    });

    useEffect(() => {
        loadSessionInfo();
        checkCameraPermission();
    }, []);

    async function loadSessionInfo() {
        const savedFullName = await SessionManager.getFullName();
        const savedRole = await SessionManager.getRole();
        const staffOrAdmin = await SessionManager.isStaffOrAdmin();

        setFullName(savedFullName);
        setRole(savedRole);
        setIsStaffOrAdmin(staffOrAdmin);
    }

    async function checkCameraPermission() {
        const status = Camera.getCameraPermissionStatus();

        if (status === 'granted') {
            setHasPermission(true);
            return;
        }

        setHasPermission(false);
    }

    async function requestCameraPermission() {
        const status = await Camera.requestCameraPermission();

        if (status === 'granted') {
            setHasPermission(true);
            setStatusMessage('Kamera izni verildi. Kamerayı başlatabilirsin.');
            return;
        }

        setHasPermission(false);
        setStatusMessage('Kamera izni verilmedi.');
        Alert.alert(
            'Kamera İzni Gerekli',
            'QR kod okutmak için kamera izni vermen gerekiyor.',
        );
    }

    function toggleScanner() {
        if (!hasPermission) {
            requestCameraPermission();
            return;
        }

        setScannerActive(prev => !prev);
        setStatusMessage(
            scannerActive
                ? 'Kamera kapatıldı.'
                : 'Kamera açık. QR kodu okutabilirsin.',
        );
    }

    async function manualCheck() {
        const code = manualCode.trim();

        if (code.length === 0) {
            Alert.alert('Uyarı', 'Bilet kodu zorunludur.');
            return;
        }

        await checkTicket(code);
    }

    async function checkTicket(code: string) {
        if (checking) {
            return;
        }

        try {
            setChecking(true);
            setStatusMessage('Bilet kontrol ediliyor...');

            setResultType('neutral');
            setResultTitle('Kontrol ediliyor...');
            setResultMessage('Bilet bilgisi backend üzerinden doğrulanıyor.');
            setCheckedTicket(null);

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.checkTicket({
                apiToken,
                ticketCode: code,
            });

            setChecking(false);
            setStatusMessage('Kontrol tamamlandı.');

            if (!response.success) {
                showFailedResult('Giriş Reddedildi', response.message);
                return;
            }

            if (!response.data) {
                showFailedResult('Bilet Kontrol Edildi', response.message);
                return;
            }

            const result = response.data.result ?? 'approved';

            if (result === 'approved') {
                showSuccessResult(response.message, response.data);
            } else {
                showFailedResult('Giriş Reddedildi', response.message);
            }
        } catch (error) {
            setChecking(false);

            const message =
                error instanceof Error ? error.message : 'Bilet kontrol edilemedi.';

            setStatusMessage(message);
            showFailedResult('Bağlantı Hatası', message);
        }
    }

    function showSuccessResult(message: string, ticket: Ticket) {
        setResultType('success');
        setResultTitle('Giriş Onaylandı');
        setResultMessage(message);
        setCheckedTicket(ticket);
    }

    function showFailedResult(title: string, message: string) {
        setResultType('failed');
        setResultTitle(title);
        setResultMessage(message);
        setCheckedTicket(null);
    }

    function getResultColor(): string {
        if (resultType === 'success') {
            return AppColors.green;
        }

        if (resultType === 'failed') {
            return AppColors.red;
        }

        return AppColors.blue;
    }

    function getResultIcon(): string {
        if (resultType === 'success') {
            return '✅';
        }

        if (resultType === 'failed') {
            return '❌';
        }

        return 'ℹ️';
    }

    function buildTicketInfo(ticket: Ticket): string {
        const ticketId = ticket.ticketId ?? ticket.ticket_id ?? ticket.id ?? 0;
        const ticketCode = ticket.ticketCode ?? ticket.ticket_code ?? '-';
        const status = ticket.ticketStatus ?? ticket.ticket_status ?? ticket.status ?? '-';

        return `Bilet ID: ${ticketId}\nBilet Kodu: ${ticketCode}\nDurum: ${status}`;
    }

    function buildUserInfo(ticket: Ticket): string {
        const ticketUser = ticket.user;

        const userFullName =
            ticketUser?.fullName ?? ticketUser?.full_name ?? '-';

        const email = ticketUser?.email ?? '-';
        const phone = ticketUser?.phone ?? '-';

        return `Kullanıcı: ${userFullName}\nE-posta: ${email}\nTelefon: ${phone}`;
    }

    function buildEventInfo(ticket: Ticket): string {
        const eventTitle =
            ticket.event?.title ?? ticket.eventTitle ?? ticket.event_title ?? '-';

        const eventDate = ticket.event?.eventDate ?? ticket.event?.event_date ?? '-';

        return `Etkinlik: ${eventTitle}\nTarih: ${eventDate}`;
    }

    function buildLocationInfo(ticket: Ticket): string {
        const cityName =
            ticket.location?.cityName ?? ticket.location?.city_name ?? ticket.city?.name ?? '-';

        const districtName =
            ticket.location?.districtName ??
            ticket.location?.district_name ??
            ticket.district?.name ??
            '-';

        const venueName =
            ticket.location?.venueName ??
            ticket.location?.venue_name ??
            ticket.venue?.name ??
            '-';

        const address =
            ticket.location?.venueAddress ??
            ticket.location?.venue_address ??
            ticket.venue?.address ??
            '-';

        return `Konum: ${cityName} / ${districtName}\nSahne: ${venueName}\nAdres: ${address}`;
    }

    return (
        <View style={styles.root}>
            <ScrollView contentContainerStyle={styles.scrollContent}>
                <View style={styles.headerCard}>
                    <Text style={styles.headerTitle}>QR Bilet Kontrol</Text>

                    <Text style={styles.headerInfo}>
                        Görevli: {fullName.length > 0 ? fullName : '-'}
                    </Text>

                    <Text style={styles.headerInfo}>Rol: {role}</Text>

                    <View style={styles.statusRow}>
                        {checking ? (
                            <ActivityIndicator size="small" color={AppColors.blue} />
                        ) : null}

                        <Text style={styles.statusText}>{statusMessage}</Text>
                    </View>
                </View>

                {!isStaffOrAdmin ? (
                    <UnauthorizedCard />
                ) : (
                    <>
                        <View style={styles.card}>
                            <Text style={styles.cardTitle}>Kamera ile QR Okut</Text>

                            <Text style={styles.cardDescription}>
                                Bilet üzerindeki QR kodu kameraya göster.
                            </Text>

                            <View style={styles.cameraBox}>
                                {device && hasPermission && scannerActive ? (
                                    <Camera
                                        style={StyleSheet.absoluteFill}
                                        device={device}
                                        isActive={scannerActive}
                                        codeScanner={codeScanner}
                                    />
                                ) : (
                                    <View style={styles.cameraPlaceholder}>
                                        <Text style={styles.cameraIcon}>📷</Text>

                                        <Text style={styles.cameraPlaceholderText}>
                                            {!hasPermission
                                                ? 'Kamera izni gerekli'
                                                : device
                                                    ? 'Kamerayı başlatmak için butona bas'
                                                    : 'Kamera bulunamadı'}
                                        </Text>
                                    </View>
                                )}

                                <View style={styles.scanFrame} />

                                <Text style={styles.scanHint}>
                                    {scannerActive
                                        ? 'QR kodu çerçevenin içine getir'
                                        : 'Kamera kapalı'}
                                </Text>
                            </View>

                            <AppButton
                                title={
                                    !hasPermission
                                        ? 'Kamera İzni Ver'
                                        : scannerActive
                                            ? 'Kamerayı Kapat'
                                            : 'Kamerayı Başlat'
                                }
                                backgroundColor={
                                    scannerActive ? AppColors.red : AppColors.green
                                }
                                onPress={toggleScanner}
                                style={styles.actionButton}
                            />
                        </View>

                        <View style={styles.card}>
                            <Text style={styles.cardTitle}>Manuel Kod Kontrolü</Text>

                            <Text style={styles.cardDescription}>
                                QR okunmazsa bilet kodunu elle girebilirsin.
                            </Text>

                            <AppTextField
                                value={manualCode}
                                placeholder="Bilet kodu"
                                onChangeText={setManualCode}
                                returnKeyType="done"
                            />

                            <AppButton
                                title="Kodu Kontrol Et"
                                loading={checking}
                                backgroundColor={AppColors.blue}
                                onPress={manualCheck}
                                style={styles.actionButton}
                            />
                        </View>

                        <View
                            style={[
                                styles.resultCard,
                                {
                                    backgroundColor: `${getResultColor()}14`,
                                },
                            ]}>
                            <View style={styles.resultHeader}>
                                <Text style={styles.resultIcon}>{getResultIcon()}</Text>

                                <Text
                                    style={[
                                        styles.resultTitle,
                                        {
                                            color: getResultColor(),
                                        },
                                    ]}>
                                    {resultTitle}
                                </Text>
                            </View>

                            <Text style={styles.resultMessage}>{resultMessage}</Text>

                            {checkedTicket ? (
                                <>
                                    <View style={styles.divider} />

                                    <InfoBlock
                                        title="Bilet Bilgisi"
                                        text={buildTicketInfo(checkedTicket)}
                                    />

                                    <InfoBlock
                                        title="Kullanıcı Bilgisi"
                                        text={buildUserInfo(checkedTicket)}
                                    />

                                    <InfoBlock
                                        title="Etkinlik Bilgisi"
                                        text={buildEventInfo(checkedTicket)}
                                    />

                                    <InfoBlock
                                        title="Konum Bilgisi"
                                        text={buildLocationInfo(checkedTicket)}
                                    />
                                </>
                            ) : null}
                        </View>
                    </>
                )}
            </ScrollView>
        </View>
    );
}

function UnauthorizedCard() {
    return (
        <View style={styles.unauthorizedCard}>
            <Text style={styles.unauthorizedIcon}>🔒</Text>

            <Text style={styles.unauthorizedTitle}>Yetkisiz Erişim</Text>

            <Text style={styles.unauthorizedText}>
                Bu ekran sadece staff veya admin hesabıyla kullanılabilir.
            </Text>
        </View>
    );
}

type InfoBlockProps = {
    title: string;
    text: string;
};

function InfoBlock({title, text}: InfoBlockProps) {
    return (
        <View style={styles.infoBlock}>
            <Text style={styles.infoBlockTitle}>{title}</Text>
            <Text style={styles.infoBlockText}>{text}</Text>
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
        marginBottom: 14,
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
    headerInfo: {
        marginTop: 6,
        fontSize: 14,
        color: AppColors.grayText,
    },
    statusRow: {
        marginTop: 10,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    statusText: {
        flex: 1,
        fontSize: 14,
        color: AppColors.grayText,
    },
    unauthorizedCard: {
        borderRadius: 18,
        padding: 22,
        alignItems: 'center',
        backgroundColor: '#FEE2E2',
    },
    unauthorizedIcon: {
        fontSize: 48,
        marginBottom: 12,
    },
    unauthorizedTitle: {
        fontSize: 22,
        fontWeight: '900',
        color: AppColors.red,
    },
    unauthorizedText: {
        marginTop: 8,
        textAlign: 'center',
        fontSize: 14,
        color: AppColors.grayText,
        lineHeight: 20,
    },
    card: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 16,
        marginBottom: 14,
        shadowColor: '#000000',
        shadowOpacity: 0.08,
        shadowRadius: 12,
        shadowOffset: {
            width: 0,
            height: 5,
        },
        elevation: 5,
    },
    cardTitle: {
        fontSize: 20,
        fontWeight: '900',
        color: AppColors.darkText,
    },
    cardDescription: {
        marginTop: 8,
        marginBottom: 14,
        fontSize: 14,
        color: AppColors.grayText,
        lineHeight: 20,
    },
    cameraBox: {
        height: 280,
        borderRadius: 18,
        overflow: 'hidden',
        backgroundColor: '#000000',
        alignItems: 'center',
        justifyContent: 'center',
    },
    cameraPlaceholder: {
        ...StyleSheet.absoluteFillObject,
        backgroundColor: '#000000',
        alignItems: 'center',
        justifyContent: 'center',
    },
    cameraIcon: {
        fontSize: 54,
        marginBottom: 10,
    },
    cameraPlaceholderText: {
        color: '#FFFFFFAA',
        fontSize: 14,
        textAlign: 'center',
    },
    scanFrame: {
        width: 210,
        height: 210,
        borderRadius: 18,
        borderWidth: 3,
        borderColor: '#FFFFFF',
    },
    scanHint: {
        position: 'absolute',
        left: 0,
        right: 0,
        bottom: 14,
        color: '#FFFFFF',
        textAlign: 'center',
        fontSize: 14,
        fontWeight: '700',
    },
    actionButton: {
        marginTop: 14,
    },
    resultCard: {
        borderRadius: 18,
        padding: 16,
        shadowColor: '#000000',
        shadowOpacity: 0.06,
        shadowRadius: 10,
        shadowOffset: {
            width: 0,
            height: 4,
        },
        elevation: 4,
    },
    resultHeader: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    resultIcon: {
        fontSize: 27,
        marginRight: 10,
    },
    resultTitle: {
        flex: 1,
        fontSize: 21,
        fontWeight: '900',
    },
    resultMessage: {
        marginTop: 10,
        fontSize: 14,
        color: AppColors.darkText,
        lineHeight: 20,
    },
    divider: {
        height: 1,
        backgroundColor: AppColors.border,
        marginVertical: 14,
    },
    infoBlock: {
        marginBottom: 12,
    },
    infoBlockTitle: {
        fontSize: 13,
        fontWeight: '900',
        color: AppColors.grayText,
        marginBottom: 4,
    },
    infoBlockText: {
        fontSize: 14,
        color: AppColors.darkText,
        lineHeight: 20,
    },
});