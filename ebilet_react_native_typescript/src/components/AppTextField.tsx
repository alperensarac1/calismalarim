import React from 'react';
import {
    KeyboardTypeOptions,
    StyleSheet,
    TextInput,
    TextInputProps,
    View,
} from 'react-native';

import {AppColors} from '../core/appColors';

type Props = {
    value: string;
    placeholder: string;
    secureTextEntry?: boolean;
    keyboardType?: KeyboardTypeOptions;
    onChangeText: (text: string) => void;
    returnKeyType?: TextInputProps['returnKeyType'];
};

export function AppTextField({
                                 value,
                                 placeholder,
                                 secureTextEntry = false,
                                 keyboardType = 'default',
                                 onChangeText,
                                 returnKeyType = 'next',
                             }: Props) {
    return (
        <View style={styles.container}>
            <TextInput
                value={value}
                placeholder={placeholder}
                placeholderTextColor="#94A3B8"
                secureTextEntry={secureTextEntry}
                keyboardType={keyboardType}
                onChangeText={onChangeText}
                returnKeyType={returnKeyType}
                autoCapitalize="none"
                autoCorrect={false}
                style={styles.input}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        width: '100%',
        height: 52,
        borderRadius: 14,
        backgroundColor: AppColors.inputBackground,
        justifyContent: 'center',
        paddingHorizontal: 14,
    },
    input: {
        color: AppColors.darkText,
        fontSize: 15,
        padding: 0,
    },
});