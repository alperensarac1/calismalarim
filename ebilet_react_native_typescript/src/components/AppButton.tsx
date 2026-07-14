import React from 'react';
import {
    ActivityIndicator,
    Pressable,
    StyleSheet,
    Text,
    ViewStyle,
} from 'react-native';

type Props = {
    title: string;
    loading?: boolean;
    backgroundColor: string;
    onPress?: () => void;
    style?: ViewStyle;
};

export function AppButton({
                              title,
                              loading = false,
                              backgroundColor,
                              onPress,
                              style,
                          }: Props) {
    return (
        <Pressable
            onPress={loading ? undefined : onPress}
            style={({pressed}) => [
                styles.button,
                {
                    backgroundColor,
                    opacity: loading || pressed ? 0.75 : 1,
                },
                style,
            ]}>
            {loading ? (
                <ActivityIndicator color="#FFFFFF" />
            ) : (
                <Text style={styles.text}>{title}</Text>
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        width: '100%',
        height: 52,
        borderRadius: 14,
        alignItems: 'center',
        justifyContent: 'center',
    },
    text: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: '700',
    },
});