import React from "react";
import {
  ActivityIndicator,
  SafeAreaView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import Slider from "@react-native-community/slider";
import ZoomableImageCanvas from "./src/editor/ZoomableImageCanvas";
import { useEditor } from "./src/editor/useEditor";

export default function App() {
  const {
    workingImage,
    workingPngBase64,
    tolerance,
    infoText,
    isProcessing,
    canUndo,
    pickImage,
    onToleranceChange,
    onImageTap,
    undo,
    reset,
    savePngToGallery,
  } = useEditor();

  return (
      <SafeAreaView style={styles.safe}>
        <View style={styles.container}>
          <Text style={styles.title}>Background Remover</Text>

          <View style={styles.row}>
            <ActionButton
                title="Fotoğraf Seç"
                onPress={pickImage}
                disabled={isProcessing}
            />
            <ActionButton
                title="Geri Al"
                onPress={undo}
                disabled={!canUndo || isProcessing}
            />
          </View>

          <View style={styles.row}>
            <ActionButton
                title="Sıfırla"
                onPress={reset}
                disabled={!workingImage || isProcessing}
            />
            <ActionButton
                title="PNG Kaydet"
                onPress={savePngToGallery}
                disabled={!workingImage || isProcessing}
            />
          </View>

          <Text style={styles.info}>{infoText}</Text>

          <Text style={styles.tolerance}>Tolerans: {Math.round(tolerance)}</Text>
          <Slider
              style={{ width: "100%", height: 40 }}
              minimumValue={0}
              maximumValue={255}
              value={tolerance}
              onValueChange={onToleranceChange}
              disabled={!workingImage}
          />

          <View style={styles.editorWrap}>
            <ZoomableImageCanvas
                imageBase64={workingPngBase64}
                imageWidth={workingImage?.width ?? 0}
                imageHeight={workingImage?.height ?? 0}
                onTapPixel={onImageTap}
            />
          </View>

          {isProcessing ? (
              <View style={styles.loading}>
                <ActivityIndicator />
              </View>
          ) : null}
        </View>
      </SafeAreaView>
  );
}

function ActionButton({
                        title,
                        onPress,
                        disabled,
                      }: {
  title: string;
  onPress: () => void | Promise<void>;
  disabled?: boolean;
}) {
  return (
      <TouchableOpacity
          style={[styles.button, disabled && styles.buttonDisabled]}
          onPress={() => void onPress()}
          disabled={disabled}
      >
        <Text style={styles.buttonText}>{title}</Text>
      </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: "#fff" },
  container: {
    flex: 1,
    padding: 12,
    gap: 10,
  },
  title: {
    fontSize: 22,
    fontWeight: "700",
    marginBottom: 4,
  },
  row: {
    flexDirection: "row",
    gap: 8,
  },
  button: {
    flex: 1,
    height: 46,
    borderRadius: 12,
    backgroundColor: "#2563eb",
    alignItems: "center",
    justifyContent: "center",
  },
  buttonDisabled: {
    backgroundColor: "#94a3b8",
  },
  buttonText: {
    color: "#fff",
    fontWeight: "700",
    fontSize: 15,
  },
  info: {
    fontSize: 15,
    color: "#1f2937",
    marginTop: 4,
  },
  tolerance: {
    fontSize: 15,
    fontWeight: "700",
    marginTop: 4,
  },
  editorWrap: {
    flex: 1,
    minHeight: 260,
  },
  loading: {
    position: "absolute",
    right: 18,
    top: 18,
    backgroundColor: "rgba(255,255,255,0.9)",
    borderRadius: 99,
    padding: 10,
  },
});
