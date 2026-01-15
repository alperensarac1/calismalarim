import React, { useMemo, useState } from 'react';
import { View, Text, StyleSheet, Image, Pressable } from 'react-native';
import { Video, ResizeMode } from 'expo-av';
import { GonderiModel } from '../service/meme_service';

export default function PostCard({item, currentUserId, baseUrl,}: {
    item: GonderiModel;
    currentUserId: number;
    baseUrl: string;
}) {
    const fullUrl = useMemo(() => baseUrl + item.media_url, [baseUrl, item.media_url]);
    const alignEnd = item.user_id === currentUserId;

    const [playing, setPlaying] = useState(false);

    return (
        <View style={[styles.wrapper, alignEnd ? { alignItems: 'flex-end' } : { alignItems: 'flex-start' }]}>
            <View style={styles.card}>
                {item.media_type === 'image' ? (
                    <Image source={{ uri: fullUrl }} style={styles.media} resizeMode="cover" />
                ) : item.media_type === 'video' ? (
                    <View style={styles.media}>
                        {!playing ? (
                            <Pressable style={styles.playOverlay} onPress={() => setPlaying(true)}>
                                <Text style={styles.playText}>▶</Text>
                            </Pressable>
                        ) : (
                            <Video
                                source={{ uri: fullUrl }}
                                style={StyleSheet.absoluteFillObject}
                                resizeMode={ResizeMode.COVER}
                                useNativeControls
                                shouldPlay
                                onPlaybackStatusUpdate={(status) => {
                                    // status: AVPlaybackStatus
                                    if (!status.isLoaded) return;
                                    if (status.didJustFinish) setPlaying(false);
                                }}
                            />
                        )}
                    </View>
                ) : (
                    <View style={[styles.media, { alignItems: 'center', justifyContent: 'center' }]}>
                        <Text>Media yok</Text>
                    </View>
                )}

                <View style={styles.metaRow}>
                    <Text style={styles.metaLeft}>Kullanıcı #{item.user_id}</Text>
                    <Text style={styles.metaRight}>{item.uploaded_at}</Text>
                </View>

                {!!item.caption && <Text style={styles.caption}>{item.caption}</Text>}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    wrapper: { width: '100%' },
    card: {
        width: 240,
        backgroundColor: 'white',
        borderRadius: 14,
        borderWidth: 1,
        borderColor: '#eee',
        padding: 8,
    },
    media: {
        width: '100%',
        height: 200,
        borderRadius: 12,
        backgroundColor: '#f3f4f6',
        overflow: 'hidden',
    },
    playOverlay: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(0,0,0,0.08)',
    },
    playText: { fontSize: 32, fontWeight: '900' },

    metaRow: { flexDirection: 'row', marginTop: 8, alignItems: 'center' },
    metaLeft: { flex: 1, fontWeight: '700' },
    metaRight: { color: '#6b7280', fontSize: 12 },

    caption: { marginTop: 6, color: '#111827' },
});
