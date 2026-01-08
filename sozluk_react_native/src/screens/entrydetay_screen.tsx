import React, { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Button,
    FlatList,
    SafeAreaView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import {RootStackParamList, Routes} from "../navigation/routes";
import {Entry} from "../models/entry";
import {SozlukApi} from "../api/sozluk_api";
import {SessionManager} from "../entity/session_manager";
import {Comment} from "../models/comment";

type Props = NativeStackScreenProps<RootStackParamList, typeof Routes.ENTRY_DETAIL>;

export default function EntryDetayScreen({ route, navigation }: Props) {
    const entryId = route.params.id;

    const [entry, setEntry] = useState<Entry | null>(null);
    const [comments, setComments] = useState<Comment[]>([]);
    const [loadingEntry, setLoadingEntry] = useState(true);
    const [loadingComments, setLoadingComments] = useState(true);
    const [posting, setPosting] = useState(false);
    const [text, setText] = useState("");

    const loadEntry = async () => {
        setLoadingEntry(true);
        try {
            const e = await SozlukApi.getEntryById(entryId);
            setEntry(e);
        } catch {
            Alert.alert("Hata", "Entry alınamadı");
        } finally {
            setLoadingEntry(false);
        }
    };

    const loadComments = async () => {
        setLoadingComments(true);
        try {
            const list = await SozlukApi.getCommentsByEntry(entryId);
            // @ts-ignore
            setComments(list ?? []);
        } catch {
            Alert.alert("Hata", "Yorumlar alınamadı");
        } finally {
            setLoadingComments(false);
        }
    };

    useEffect(() => {
        loadEntry();
        loadComments();
    }, [entryId]);

    const addComment = async () => {
        const v = text.trim();
        if (!v) return;

        setPosting(true);
        try {
            const userId = await SessionManager.getUserId();
            const res = await SozlukApi.addComment({
                entry_id: String(entryId),
                user_id: String(userId),
                comment_text: v,
            });

            if (res.success) {
                setText("");
                await loadComments();
            } else {
                Alert.alert("Hata", res.message ?? "Yorum eklenemedi");
            }
        } catch {
            Alert.alert("Hata", "Bağlantı hatası");
        } finally {
            setPosting(false);
        }
    };

    const openVoteDialog = async (c: Comment) => {
        const userId = await SessionManager.getUserId();

        Alert.alert(
            "Yorumu Oyla",
            c.comment_text,
            [
                {
                    text: "👎 Beğenme",
                    onPress: async () => {
                        try {
                            await SozlukApi.voteComment({ comment_id: c.id, user_id: userId, is_like: 0 });
                            await loadComments();
                        } catch {}
                    },
                },
                {
                    text: "👍 Beğen",
                    onPress: async () => {
                        try {
                            await SozlukApi.voteComment({ comment_id: c.id, user_id: userId, is_like: 1 });
                            await loadComments();
                        } catch {}
                    },
                },
                { text: "İptal", style: "cancel" },
            ],
            { cancelable: true }
        );
    };

    return (
        <SafeAreaView style={styles.page}>
            <Button title="Geri" onPress={() => navigation.goBack()} />

            {loadingEntry ? (
                <View style={styles.center}><ActivityIndicator /></View>
            ) : (
                entry && (
                    <View style={styles.entryBox}>
                        <Text style={styles.title}>{entry.title}</Text>
                        <Text style={styles.content}>{entry.content}</Text>
                        <Text style={styles.meta}>
                            {[entry.username, entry.created_at?.slice(0, 10)].filter(Boolean).join(" • ")}
                        </Text>
                    </View>
                )
            )}

            <Text style={styles.h2}>Yorumlar</Text>

            {loadingComments ? (
                <View style={styles.center}><ActivityIndicator /></View>
            ) : (
                <FlatList
                    data={comments}
                    keyExtractor={(c) => String(c.id)}
                    ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
                    renderItem={({ item }) => (
                        <View style={styles.commentCard}>
                            <Text style={styles.commentUser}>{item.username}</Text>
                            <Text style={styles.commentText} onPress={() => openVoteDialog(item)}>
                                {item.comment_text}
                            </Text>
                            <Text style={styles.commentMeta}>👍{item.likes}   👎{item.dislikes}</Text>
                        </View>
                    )}
                    ListEmptyComponent={<Text>Henüz yorum yok</Text>}
                    contentContainerStyle={{ paddingBottom: 12 }}
                />
            )}

            <TextInput
                value={text}
                onChangeText={setText}
                placeholder="Yorum yaz"
                style={styles.input}
            />
            <Button title={posting ? "Gönderiliyor..." : "Gönder"} onPress={addComment} disabled={posting} />
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    page: { flex: 1, padding: 16, gap: 10 },
    center: { alignItems: "center", justifyContent: "center" },
    entryBox: { borderWidth: 1, borderColor: "#ddd", borderRadius: 12, padding: 12, backgroundColor: "white" },
    title: { fontSize: 18, fontWeight: "800" },
    content: { marginTop: 6, fontSize: 15 },
    meta: { marginTop: 8, color: "#666" },
    h2: { fontSize: 16, fontWeight: "700", marginTop: 6 },
    commentCard: { borderWidth: 1, borderColor: "#eee", borderRadius: 12, padding: 12, backgroundColor: "white" },
    commentUser: { fontWeight: "700" },
    commentText: { marginTop: 6 },
    commentMeta: { marginTop: 8, color: "#444" },
    input: { borderWidth: 1, borderColor: "#bbb", borderRadius: 10, padding: 12 },
});
