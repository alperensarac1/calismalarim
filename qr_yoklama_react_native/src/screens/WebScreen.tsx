import React, { useState } from "react";
import { ActivityIndicator, View } from "react-native";
import { WebView } from "react-native-webview";
import { AppToast } from "../ui/toast";

type Props = {
    route: { params: { title: string; url: string } };
    navigation: any;
};

export default function WebScreen({ route }: Props) {
    const { url } = route.params;
    const [loading, setLoading] = useState(true);

    return (
        <View style={{ flex: 1 }}>
            <WebView
                source={{ uri: url }}
                onLoadStart={() => setLoading(true)}
                onLoadEnd={() => setLoading(false)}
                onError={() => {
                    setLoading(false);
                    AppToast.error("WebView yüklenemedi");
                }}
            />
            {loading ? (
                <View
                    style={{
                        position: "absolute",
                        left: 0,
                        right: 0,
                        top: 0,
                        bottom: 0,
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <ActivityIndicator size="large" />
                </View>
            ) : null}
        </View>
    );
}
