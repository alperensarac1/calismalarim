// src/screens/MainScreen.tsx
import React, { useMemo, useRef } from "react";
import {
    View,
    Text,
    StyleSheet,
    Pressable,
    FlatList,
    Animated,
    Easing,
    Dimensions,
} from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import {useGameStore} from "../data/game_store";
import {FloatingText} from "../model/floating_text";
import {Upgrade} from "../model/upgrade";
import {RootStackParamList} from "../../App";


const { width } = Dimensions.get("window");

export default function MainScreen({ navigation }: NativeStackScreenProps<RootStackParamList, "Main">) {
    const game = useGameStore(s => s.game);
    const perks = useGameStore(s => s.perks);
    const upgrades = useGameStore(s => s.upgrades);
    const floaters = useGameStore(s => s.floaters);
    const critReady = useGameStore(s => s.critReady);
    const critLeft = useGameStore(s => s.critCooldownLeft);

    const onTapCookie = useGameStore(s => s.onTapCookie);
    const buyUpgrade = useGameStore(s => s.buyUpgrade);
    const prestige = useGameStore(s => s.prestige);
    const reset = useGameStore(s => s.reset);
    const doCrit = useGameStore(s => s.doCrit);

    const scale = useRef(new Animated.Value(1)).current;

    const discountPct = useMemo(() => Math.min(perks.discount * 0.02, 0.5), [perks.discount]);

    const runCookieAnim = () => {
        scale.setValue(1);
        Animated.sequence([
            Animated.timing(scale, { toValue: 0.92, duration: 80, useNativeDriver: true, easing: Easing.out(Easing.quad) }),
            Animated.timing(scale, { toValue: 1, duration: 120, useNativeDriver: true, easing: Easing.out(Easing.quad) }),
        ]).start();
    };

    const renderUpgrade = ({ item }: { item: Upgrade }) => {
        const price = item.basePrice * Math.pow(item.priceMultiplier ?? 1.15, item.level) * (1 - discountPct);
        const can = game.score >= price;

        return (
            <View style={styles.upCard}>
                <Text style={styles.upTitle}>
                    {item.level > 0 ? `${item.title} (Lv ${item.level})` : item.title}
                </Text>
                <Text style={styles.upDesc}>{item.desc}</Text>

                <View style={styles.row}>
                    <Text style={styles.price}>{formatNum(price)}</Text>
                    <Pressable
                        onPress={() => buyUpgrade(item.id)}
                        style={[styles.buyBtn, !can && styles.buyBtnDisabled]}
                        disabled={!can}
                    >
                        <Text style={styles.buyBtnText}>Buy</Text>
                    </Pressable>
                </View>
            </View>
        );
    };

    return (
        <View style={styles.root}>
            <View style={styles.topCard}>
                <Text style={styles.score}>{formatNum(game.score)}</Text>
                <Text style={styles.cps}>{formatNum(game.cps)} / sn</Text>
            </View>

            <View style={styles.cookieArea}>
                <Pressable
                    onPress={(e) => {
                        runCookieAnim();
                        const { locationX, locationY } = e.nativeEvent;
                        onTapCookie(locationX, locationY);
                    }}
                >
                    <Animated.View style={[styles.cookie, { transform: [{ scale }] }]}>
                        <Text style={styles.cookieEmoji}>🍪</Text>
                    </Animated.View>
                </Pressable>
            </View>

            <FlatList
                data={upgrades}
                keyExtractor={(u) => String(u.id)}
                contentContainerStyle={styles.listPad}
                ListHeaderComponent={<Text style={styles.header}>Yükseltmeler</Text>}
                renderItem={renderUpgrade}
            />

            <View style={styles.bottomBar}>
                <Pressable style={styles.btn} onPress={() => navigation.navigate("Shop")}>
                    <Text style={styles.btnText}>Shop</Text>
                </Pressable>
                <Pressable style={[styles.btn, styles.btnOutline]} onPress={prestige}>
                    <Text style={styles.btnText}>Prestige</Text>
                </Pressable>
                <Pressable style={[styles.btn, styles.btnOutline]} onPress={reset}>
                    <Text style={styles.btnText}>Reset</Text>
                </Pressable>
                <Pressable
                    style={[styles.btn, styles.btnOutline, !critReady && styles.btnDisabled]}
                    onPress={() => doCrit(110, 110)}
                    disabled={!critReady}
                >
                    <Text style={styles.btnText}>{critReady ? "Crit" : `${critLeft}s`}</Text>
                </Pressable>
            </View>

            {floaters.map((f) => (
                <Floater key={f.id} f={f} />
            ))}
        </View>
    );
}

function Floater({ f }: { f: FloatingText }) {
    const y = useRef(new Animated.Value(f.y)).current;
    const op = useRef(new Animated.Value(1)).current;

    React.useEffect(() => {
        Animated.parallel([
            Animated.timing(y, { toValue: Math.max(0, f.y - 80), duration: 650, useNativeDriver: false }),
            Animated.timing(op, { toValue: 0, duration: 650, useNativeDriver: false }),
        ]).start();
    }, [f.y, y, op]);

    return (
        <Animated.View
            pointerEvents="none"
            style={[
                styles.floater,
                {
                    left: f.x,
                    top: Math.max(0, f.y - 120),
                    opacity: op,
                    transform: [{ translateY: Animated.subtract(y, f.y) }],
                },
            ]}
        >
            <Text style={[styles.floaterText, { color: f.isCrit ? "#ff3b30" : "#fff" }]}>{f.text}</Text>
        </Animated.View>
    );
}

function formatNum(v: number): string {
    if (v >= 1_000_000) return `${(v / 1_000_000).toFixed(2)}M`;
    if (v >= 1_000) return `${(v / 1_000).toFixed(1)}k`;
    return `${Math.floor(v)}`;
}

const styles = StyleSheet.create({
    root: {
        flex: 1,
        backgroundColor: "#FFF3E5",
    },
    topCard: {
        margin: 16,
        padding: 16,
        borderRadius: 14,
        backgroundColor: "#fff",
        shadowColor: "#000",
        shadowOpacity: 0.06,
        shadowRadius: 6,
        elevation: 2,
    },
    score: { fontSize: 30, fontWeight: "800" },
    cps: { marginTop: 4, color: "#333" },

    cookieArea: { height: 300, alignItems: "center", justifyContent: "center" },
    cookie: {
        width: 220,
        height: 220,
        borderRadius: 110,
        backgroundColor: "#CE9B62",
        alignItems: "center",
        justifyContent: "center",
    },
    cookieEmoji: { fontSize: 64 },

    header: { fontSize: 18, fontWeight: "700", marginBottom: 8 },
    listPad: { paddingHorizontal: 16, paddingBottom: 90 },

    upCard: {
        backgroundColor: "#fff",
        borderRadius: 14,
        padding: 12,
        marginVertical: 6,
        shadowColor: "#000",
        shadowOpacity: 0.05,
        shadowRadius: 6,
        elevation: 1,
    },
    upTitle: { fontWeight: "700", fontSize: 15 },
    upDesc: { marginTop: 2, color: "#444" },
    row: { marginTop: 10, flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    price: { fontWeight: "700" },
    buyBtn: { paddingVertical: 8, paddingHorizontal: 14, borderRadius: 10, borderWidth: 1, borderColor: "#222" },
    buyBtnDisabled: { opacity: 0.4 },
    buyBtnText: { fontWeight: "700" },

    bottomBar: {
        position: "absolute",
        left: 0,
        right: 0,
        bottom: 0,
        height: 64,
        paddingHorizontal: 12,
        backgroundColor: "rgba(255,255,255,0.25)",
        flexDirection: "row",
        alignItems: "center",
        gap: 8 as any,
    },
    btn: {
        flex: 1,
        height: 44,
        borderRadius: 12,
        backgroundColor: "#222",
        alignItems: "center",
        justifyContent: "center",
    },
    btnOutline: { backgroundColor: "transparent", borderWidth: 1, borderColor: "#222" },
    btnDisabled: { opacity: 0.4 },
    btnText: { color: "#fff", fontWeight: "800" },

    floater: {
        position: "absolute",
        paddingHorizontal: 6,
        paddingVertical: 2,
        backgroundColor: "rgba(0,0,0,0.4)",
        borderRadius: 6,
    },
    floaterText: { fontWeight: "800" },
});
