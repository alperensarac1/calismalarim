import React, { useEffect, useMemo, useState } from 'react';
import { View, Text, StyleSheet, FlatList, Pressable, Alert, Modal, TextInput, Image } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import * as ImagePicker from 'expo-image-picker';

import { RootStackParamList, Routes } from '../navigation/routes';
import { useMedia } from '../store/media_context';
import PostCard from './post_card';

type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.ODA>;

type Picked = {
    isVideo: boolean;
    uri: string;
    base64?: string; // sadece image için
    type: string;    // mime
    fileName: string;
};

export default function OdaScreen({ route }: Props) {
    const { roomId, userId } = route.params;
    const { state, fetchPosts, uploadImage, uploadVideo } = useMedia();

    const [shareVisible, setShareVisible] = useState(false);
    const [caption, setCaption] = useState('');
    const [picked, setPicked] = useState<Picked | null>(null);

    const baseUrl = useMemo(() => 'https://alperensaracdeneme.com/meme/', []);

    useEffect(() => {
        fetchPosts(roomId);
    }, [roomId]);

    const openPicker = async () => {
        const perm = await ImagePicker.requestMediaLibraryPermissionsAsync();
        if (!perm.granted) {
            Alert.alert('İzin gerekli', 'Galeri izni verilmedi');
            return;
        }

        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ImagePicker.MediaTypeOptions.All, // image + video
            quality: 0.85,
            base64: true, // image base64 için
        });

        if (result.canceled) return;

        const asset = result.assets?.[0];
        if (!asset?.uri) return;

        const isVideo = asset.type === 'video';

        // expo asset.type: 'image' | 'video'
        // mime bazen gelmeyebilir, kendimiz setliyoruz
        const mime = isVideo ? 'video/mp4' : 'image/jpeg';

        const fileName = isVideo ? 'video.mp4' : 'image.jpg';

        const p: Picked = {
            isVideo,
            uri: asset.uri,
            base64: !isVideo ? asset.base64 ?? undefined : undefined,
            type: mime,
            fileName,
        };

        setPicked(p);
        setCaption('');
        setShareVisible(true);
    };

    const sendShare = async () => {
        if (!picked) return;

        const cap = caption.trim();

        if (picked.isVideo) {
            const file = {
                uri: picked.uri,
                name: picked.fileName,
                type: picked.type || 'video/mp4',
            };

            const r = await uploadVideo({
                roomId,
                userId,
                caption: cap,
                file,
            });

            setShareVisible(false);
            setPicked(null);

            if (r.ok) {
                Alert.alert('Başarılı', '✅ Video başarıyla yüklendi');
                fetchPosts(roomId);
            } else {
                Alert.alert('Hata', r.message || 'Video yükleme başarısız');
            }
        } else {
            if (!picked.base64) {
                setShareVisible(false);
                setPicked(null);
                return Alert.alert('Hata', 'Resim base64 alınamadı');
            }

            const r = await uploadImage({
                roomId,
                userId,
                caption: cap,
                base64: picked.base64,
            });

            setShareVisible(false);
            setPicked(null);

            if (r.ok) {
                Alert.alert('Başarılı', '✅ Görsel başarıyla yüklendi');
                fetchPosts(roomId);
            } else {
                Alert.alert('Hata', r.message || 'Görsel yükleme başarısız');
            }
        }
    };

    return (
        <View style={styles.container}>
            {state.isLoadingList ? (
                <Text style={{ textAlign: 'center', marginTop: 20 }}>Yükleniyor...</Text>
            ) : state.posts.length === 0 ? (
                <Text style={{ textAlign: 'center', marginTop: 20 }}>Henüz gönderi yok</Text>
            ) : (
                <FlatList
                    data={state.posts}
                    keyExtractor={(item) => String(item.id)}
                    contentContainerStyle={{ paddingVertical: 12 }}
                    ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
                    renderItem={({ item }) => <PostCard item={item} currentUserId={userId} baseUrl={baseUrl} />}
                />
            )}

            <Pressable
                style={[styles.fab, (state.isUploading || state.isLoadingList) && { opacity: 0.6 }]}
                onPress={openPicker}
                disabled={state.isUploading}
            >
                <Text style={styles.fabText}>{state.isUploading ? '...' : 'Paylaş'}</Text>
            </Pressable>

            <Modal visible={shareVisible} transparent animationType="fade" onRequestClose={() => setShareVisible(false)}>
                <View style={styles.modalBackdrop}>
                    <View style={styles.modalCard}>
                        <Text style={styles.modalTitle}>Paylaş</Text>

                        {picked?.isVideo ? (
                            <View style={styles.previewBox}>
                                <Text>Video seçildi</Text>
                            </View>
                        ) : picked?.uri ? (
                            <Image source={{ uri: picked.uri }} style={styles.previewImg} resizeMode="cover" />
                        ) : null}

                        <TextInput style={styles.input} placeholder="Açıklama" value={caption} onChangeText={setCaption} />

                        <View style={{ flexDirection: 'row', gap: 10, marginTop: 10 }}>
                            <Pressable
                                style={[styles.modalBtn, { backgroundColor: '#111827' }]}
                                onPress={() => setShareVisible(false)}
                                disabled={state.isUploading}
                            >
                                <Text style={styles.modalBtnText}>İptal</Text>
                            </Pressable>

                            <Pressable
                                style={[styles.modalBtn, { backgroundColor: '#6d28d9', opacity: state.isUploading ? 0.6 : 1 }]}
                                onPress={sendShare}
                                disabled={state.isUploading}
                            >
                                <Text style={styles.modalBtnText}>{state.isUploading ? 'Yükleniyor...' : 'Gönder'}</Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </Modal>
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, paddingHorizontal: 12 },

    fab: {
        position: 'absolute',
        right: 16,
        bottom: 16,
        backgroundColor: '#6d28d9',
        paddingHorizontal: 18,
        paddingVertical: 14,
        borderRadius: 999,
    },
    fabText: { color: 'white', fontWeight: '900' },

    modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', padding: 20 },
    modalCard: { backgroundColor: 'white', borderRadius: 14, padding: 16 },
    modalTitle: { fontSize: 18, fontWeight: '900', marginBottom: 10 },
    input: { borderWidth: 1, borderColor: '#ddd', borderRadius: 12, padding: 12, marginTop: 12 },

    previewImg: { width: '100%', height: 200, borderRadius: 12, backgroundColor: '#f3f4f6' },
    previewBox: { width: '100%', height: 200, borderRadius: 12, backgroundColor: '#f3f4f6', alignItems: 'center', justifyContent: 'center' },

    modalBtn: { flex: 1, padding: 12, borderRadius: 12, alignItems: 'center' },
    modalBtnText: { color: 'white', fontWeight: '900' },
});
