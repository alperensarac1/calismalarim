import React, {useEffect, useState} from 'react';
import {
    ActivityIndicator,
    Alert,
    Pressable,
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
import {City} from '../../models/City';
import {District} from '../../models/District';
import {Event} from '../../models/Event';
import {AppButton} from '../../components/AppButton';
import {EventCard} from '../../components/EventCard';
import {RootStackParamList} from '../../navigation/routes';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

export function HomeScreen({navigation}: Props) {
    const [cities, setCities] = useState<City[]>([]);
    const [districts, setDistricts] = useState<District[]>([]);
    const [events, setEvents] = useState<Event[]>([]);

    const [selectedCity, setSelectedCity] = useState<City | null>(null);
    const [selectedDistrict, setSelectedDistrict] = useState<District | null>(
        null,
    );

    const [fullName, setFullName] = useState('');
    const [role, setRole] = useState('user');
    const [isStaffOrAdmin, setIsStaffOrAdmin] = useState(false);

    const [statusMessage, setStatusMessage] = useState('Şehirler yükleniyor...');

    const [loadingCities, setLoadingCities] = useState(false);
    const [loadingDistricts, setLoadingDistricts] = useState(false);
    const [loadingEvents, setLoadingEvents] = useState(false);

    useEffect(() => {
        loadSessionInfo();
        loadCities();
    }, []);

    async function loadSessionInfo() {
        const savedFullName = await SessionManager.getFullName();
        const savedRole = await SessionManager.getRole();
        const staffOrAdmin = await SessionManager.isStaffOrAdmin();

        setFullName(savedFullName);
        setRole(savedRole);
        setIsStaffOrAdmin(staffOrAdmin);
    }

    async function loadCities() {
        try {
            setLoadingCities(true);
            setStatusMessage('Şehirler yükleniyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getCities(apiToken);

            setLoadingCities(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setCities(list);
            setSelectedCity(null);
            setSelectedDistrict(null);
            setDistricts([]);
            setEvents([]);

            if (list.length === 0) {
                setStatusMessage('Aktif şehir bulunamadı.');
            } else {
                setStatusMessage('Şehir seçiniz.');
            }
        } catch (error) {
            setLoadingCities(false);

            const message =
                error instanceof Error ? error.message : 'Şehirler yüklenemedi.';

            setStatusMessage(message);
        }
    }

    async function loadDistricts(city: City) {
        try {
            setLoadingDistricts(true);
            setStatusMessage('İlçeler yükleniyor...');

            setSelectedCity(city);
            setSelectedDistrict(null);
            setDistricts([]);
            setEvents([]);

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getDistrictsByCity({
                apiToken,
                cityId: city.id,
            });

            setLoadingDistricts(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setDistricts(list);

            if (list.length === 0) {
                setStatusMessage('Bu şehir için aktif ilçe bulunamadı.');
            } else {
                setStatusMessage('İlçe seçip etkinlikleri listeleyebilirsin.');
            }
        } catch (error) {
            setLoadingDistricts(false);

            const message =
                error instanceof Error ? error.message : 'İlçeler yüklenemedi.';

            setStatusMessage(message);
        }
    }

    async function loadEvents() {
        if (!selectedCity) {
            Alert.alert('Uyarı', 'Lütfen şehir seçiniz.');
            return;
        }

        if (!selectedDistrict) {
            Alert.alert('Uyarı', 'Lütfen ilçe seçiniz.');
            return;
        }

        try {
            setLoadingEvents(true);
            setStatusMessage('Etkinlikler yükleniyor...');

            const apiToken = await SessionManager.getApiToken();

            const response = await ApiService.getEventsByLocation({
                apiToken,
                cityId: selectedCity.id,
                districtId: selectedDistrict.id,
            });

            setLoadingEvents(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setEvents(list);

            if (list.length === 0) {
                setStatusMessage('Bu konum için etkinlik bulunamadı.');
            } else {
                setStatusMessage(`${list.length} etkinlik listelendi.`);
            }
        } catch (error) {
            setLoadingEvents(false);

            const message =
                error instanceof Error ? error.message : 'Etkinlikler yüklenemedi.';

            setStatusMessage(message);
        }
    }

    async function logout() {
        await SessionManager.logout();

        navigation.reset({
            index: 0,
            routes: [{name: 'Login'}],
        });
    }

    function openMyTickets() {
        navigation.navigate('MyTickets');
    }

    function openScanner() {
        Alert.alert('Bilgi', 'QR Kontrol ekranı sonraki adımda yapılacak.');
    }

    function openEventDetail(event: Event) {
        navigation.navigate('EventDetail', {
            eventId: event.id,
        });
    }

    function roleText() {
        if (role === 'admin') {
            return 'Admin hesabı';
        }

        if (role === 'staff') {
            return 'Görevli hesabı';
        }

        return 'Etkinlikleri keşfet';
    }

    const isAnyLoading = loadingCities || loadingDistricts || loadingEvents;

    return (
        <View style={styles.root}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={
                    <RefreshControl refreshing={loadingCities} onRefresh={loadCities} />
                }>
                <View style={styles.headerCard}>
                    <Text style={styles.welcomeText}>
                        Hoş geldin, {fullName.length > 0 ? fullName : 'Kullanıcı'}
                    </Text>

                    <Text style={styles.roleText}>{roleText()}</Text>

                    <View style={styles.headerButtonsRow}>
                        <SmallHeaderButton
                            title="Biletlerim"
                            backgroundColor={AppColors.blue}
                            onPress={openMyTickets}
                        />

                        {isStaffOrAdmin ? (
                            <SmallHeaderButton
                                title="QR Kontrol"
                                backgroundColor={AppColors.green}
                                onPress={openScanner}
                            />
                        ) : null}

                        <SmallHeaderButton
                            title="Çıkış"
                            backgroundColor={AppColors.red}
                            onPress={logout}
                        />
                    </View>
                </View>

                <View style={styles.filterCard}>
                    <Text style={styles.filterTitle}>Konum Seç</Text>

                    <Text style={styles.filterSubtitle}>
                        Önce şehir, sonra ilçe seçerek etkinlikleri listeleyebilirsin.
                    </Text>

                    <Text style={styles.label}>Şehir</Text>

                    <View style={styles.selectBox}>
                        {cities.length === 0 ? (
                            <Text style={styles.placeholderText}>
                                {loadingCities ? 'Şehirler yükleniyor...' : 'Şehir bulunamadı'}
                            </Text>
                        ) : (
                            cities.map(city => {
                                const selected = selectedCity?.id === city.id;

                                return (
                                    <Pressable
                                        key={city.id}
                                        onPress={() => loadDistricts(city)}
                                        style={[
                                            styles.optionChip,
                                            selected && styles.optionChipSelected,
                                        ]}>
                                        <Text
                                            style={[
                                                styles.optionChipText,
                                                selected && styles.optionChipTextSelected,
                                            ]}>
                                            {city.name}
                                        </Text>
                                    </Pressable>
                                );
                            })
                        )}
                    </View>

                    <Text style={styles.label}>İlçe</Text>

                    <View style={styles.selectBox}>
                        {selectedCity === null ? (
                            <Text style={styles.placeholderText}>Önce şehir seçiniz</Text>
                        ) : loadingDistricts ? (
                            <Text style={styles.placeholderText}>İlçeler yükleniyor...</Text>
                        ) : districts.length === 0 ? (
                            <Text style={styles.placeholderText}>İlçe bulunamadı</Text>
                        ) : (
                            districts.map(district => {
                                const selected = selectedDistrict?.id === district.id;

                                return (
                                    <Pressable
                                        key={district.id}
                                        onPress={() => setSelectedDistrict(district)}
                                        style={[
                                            styles.optionChip,
                                            selected && styles.optionChipSelected,
                                        ]}>
                                        <Text
                                            style={[
                                                styles.optionChipText,
                                                selected && styles.optionChipTextSelected,
                                            ]}>
                                            {district.name}
                                        </Text>
                                    </Pressable>
                                );
                            })
                        )}
                    </View>

                    <AppButton
                        title="Etkinlikleri Listele"
                        loading={loadingEvents}
                        backgroundColor={AppColors.green}
                        onPress={loadEvents}
                        style={styles.listButton}
                    />
                </View>

                <View style={styles.statusRow}>
                    {isAnyLoading ? (
                        <ActivityIndicator size="small" color={AppColors.blue} />
                    ) : null}

                    <Text style={styles.statusText}>{statusMessage}</Text>
                </View>

                {events.length === 0 && !isAnyLoading ? <EmptyEventsCard /> : null}

                {events.map(event => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onPress={() => openEventDetail(event)}
                    />
                ))}
            </ScrollView>
        </View>
    );
}

type SmallButtonProps = {
    title: string;
    backgroundColor: string;
    onPress: () => void;
};

function SmallHeaderButton({title, backgroundColor, onPress}: SmallButtonProps) {
    return (
        <Pressable
            onPress={onPress}
            style={({pressed}) => [
                styles.smallButton,
                {
                    backgroundColor,
                    opacity: pressed ? 0.75 : 1,
                },
            ]}>
            <Text style={styles.smallButtonText}>{title}</Text>
        </Pressable>
    );
}

function EmptyEventsCard() {
    return (
        <View style={styles.emptyCard}>
            <Text style={styles.emptyIcon}>📅</Text>

            <Text style={styles.emptyTitle}>Henüz etkinlik listelenmedi</Text>

            <Text style={styles.emptyDescription}>
                Şehir ve ilçe seçtikten sonra etkinlikleri listeleyebilirsin.
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
    welcomeText: {
        fontSize: 22,
        fontWeight: '800',
        color: AppColors.darkText,
    },
    roleText: {
        marginTop: 6,
        fontSize: 14,
        color: AppColors.grayText,
    },
    headerButtonsRow: {
        flexDirection: 'row',
        gap: 8,
        marginTop: 14,
    },
    smallButton: {
        flex: 1,
        height: 42,
        borderRadius: 12,
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: 4,
    },
    smallButtonText: {
        color: '#FFFFFF',
        fontSize: 13,
        fontWeight: '800',
    },
    filterCard: {
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
    filterTitle: {
        fontSize: 20,
        fontWeight: '800',
        color: AppColors.darkText,
    },
    filterSubtitle: {
        marginTop: 6,
        fontSize: 14,
        color: AppColors.grayText,
        lineHeight: 20,
    },
    label: {
        marginTop: 14,
        marginBottom: 7,
        fontSize: 13,
        color: AppColors.grayText,
        fontWeight: '700',
    },
    selectBox: {
        minHeight: 52,
        borderRadius: 14,
        backgroundColor: AppColors.inputBackground,
        padding: 8,
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
        alignItems: 'center',
    },
    placeholderText: {
        color: AppColors.grayText,
        fontSize: 14,
        paddingHorizontal: 6,
    },
    optionChip: {
        borderRadius: 999,
        paddingHorizontal: 12,
        paddingVertical: 8,
        backgroundColor: '#FFFFFF',
        borderWidth: 1,
        borderColor: AppColors.border,
    },
    optionChipSelected: {
        backgroundColor: AppColors.blue,
        borderColor: AppColors.blue,
    },
    optionChipText: {
        color: AppColors.darkText,
        fontSize: 14,
        fontWeight: '600',
    },
    optionChipTextSelected: {
        color: '#FFFFFF',
    },
    listButton: {
        marginTop: 16,
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
    emptyCard: {
        backgroundColor: AppColors.cardBackground,
        borderRadius: 18,
        padding: 22,
        marginBottom: 14,
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
    emptyIcon: {
        fontSize: 44,
        marginBottom: 10,
    },
    emptyTitle: {
        fontSize: 17,
        fontWeight: '800',
        color: AppColors.darkText,
        textAlign: 'center',
    },
    emptyDescription: {
        marginTop: 6,
        fontSize: 14,
        color: AppColors.grayText,
        textAlign: 'center',
        lineHeight: 20,
    },
});