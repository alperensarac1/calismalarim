import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, Pressable, StyleSheet, Alert, Modal, TextInput } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import {RootStackParamList, Routes} from "../navigation/routes";
import {useRooms} from "../store/rooms_context";


type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.HOME>;

export default function HomeScreen({ navigation, route }: Props) {
    const { userId } = route.params;
    const { state, fetchRooms, createRoom, joinRoom } = useRooms();

    const [joinVisible, setJoinVisible] = useState(false);
    const [roomCode, setRoomCode] = useState('');

    useEffect(() => {
        fetchRooms(userId);
    }, [userId]);

    const onCreate = async () => {
        const res = await createRoom(userId);
        if (res.ok) {
            Alert.alert('Başarılı', `Oda oluşturuldu: ${res.roomCode ?? ''}`);
            fetchRooms(userId);
        } else {
            Alert.alert('Hata', res.message || state.error || 'Oda oluşturma başarısız');
        }
    };

    const onJoin = async () => {
        const code = roomCode.trim();
        if (!code) return;

        const res = await joinRoom(userId, code);
        setJoinVisible(false);
        setRoomCode('');

        if (res.ok) {
            Alert.alert('Başarılı', 'Odaya katıldınız');
            fetchRooms(userId);
        } else {
            Alert.alert('Hata', res.message || state.error || 'Katılım başarısız');
        }
    };

    return (
        <View style={styles.container}>
            <Pressable style={styles.joinBtn} onPress={() => setJoinVisible(true)}>
                <Text style={styles.joinBtnText}>Odaya Katıl (Kod ile)</Text>
            </Pressable>

            {state.isLoadingRooms ? (
                <Text>Yükleniyor...</Text>
            ) : state.rooms.length === 0 ? (
                <Text style={{ textAlign: 'center', marginTop: 20 }}>Henüz oda yok</Text>
            ) : (
                <FlatList
                    data={state.rooms}
                    keyExtractor={(item) => String(item.room_id)}
                    ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
                    renderItem={({ item }) => (
                        <Pressable
                            style={styles.card}
                            onPress={() => navigation.navigate(Routes.ODA, { roomId: item.room_id, userId })}
                        >
                            <Text style={styles.cardTitle}>{item.room_code}</Text>
                            <Text style={styles.cardSub}>Oluşturan: {item.created_by}</Text>
                        </Pressable>
                    )}
                />
            )}

            <Pressable style={[styles.fab, state.isLoadingCreate && { opacity: 0.6 }]} onPress={onCreate} disabled={state.isLoadingCreate}>
                <Text style={styles.fabText}>{state.isLoadingCreate ? '...' : 'Oda Oluştur'}</Text>
            </Pressable>

            {/* Join Dialog */}
            <Modal visible={joinVisible} transparent animationType="fade" onRequestClose={() => setJoinVisible(false)}>
                <View style={styles.modalBackdrop}>
                    <View style={styles.modalCard}>
                        <Text style={styles.modalTitle}>Oda Katılım</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="Oda Kodu"
                            value={roomCode}
                            onChangeText={setRoomCode}
                            autoCapitalize="characters"
                        />
                        <View style={{ flexDirection: 'row', gap: 10, marginTop: 10 }}>
                            <Pressable style={[styles.modalBtn, { backgroundColor: '#111827' }]} onPress={() => setJoinVisible(false)}>
                                <Text style={styles.modalBtnText}>İptal</Text>
                            </Pressable>
                            <Pressable style={[styles.modalBtn, { backgroundColor: '#6d28d9' }]} onPress={onJoin}>
                                <Text style={styles.modalBtnText}>Katıl</Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </Modal>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, padding: 16 },
    joinBtn: { backgroundColor: '#111827', padding: 14, borderRadius: 12, marginBottom: 12 },
    joinBtnText: { color: 'white', fontWeight: '800', textAlign: 'center' },

    card: { padding: 16, borderRadius: 14, borderWidth: 1, borderColor: '#eee' },
    cardTitle: { fontSize: 18, fontWeight: '800' },
    cardSub: { marginTop: 6, color: '#6b7280' },

    fab: { position: 'absolute', right: 16, bottom: 16, backgroundColor: '#6d28d9', paddingHorizontal: 18, paddingVertical: 14, borderRadius: 999 },
    fabText: { color: 'white', fontWeight: '900' },

    modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', padding: 20 },
    modalCard: { backgroundColor: 'white', borderRadius: 14, padding: 16 },
    modalTitle: { fontSize: 18, fontWeight: '800', marginBottom: 10 },
    input: { borderWidth: 1, borderColor: '#ddd', borderRadius: 12, padding: 12 },

    modalBtn: { flex: 1, padding: 12, borderRadius: 12, alignItems: 'center' },
    modalBtnText: { color: 'white', fontWeight: '800' },
});
