import { fromByteArray, toByteArray } from "base64-js";
import jpeg from "jpeg-js";
import UPNG from "upng-js";
import type { RgbaImage } from "./types";

function isPng(bytes: Uint8Array): boolean {
    return (
        bytes.length >= 8 &&
        bytes[0] === 0x89 &&
        bytes[1] === 0x50 &&
        bytes[2] === 0x4e &&
        bytes[3] === 0x47
    );
}

function isJpeg(bytes: Uint8Array): boolean {
    return bytes.length >= 2 && bytes[0] === 0xff && bytes[1] === 0xd8;
}

function toExactArrayBuffer(bytes: Uint8Array): ArrayBuffer {
    return bytes.buffer.slice(
        bytes.byteOffset,
        bytes.byteOffset + bytes.byteLength
    ) as ArrayBuffer;
}

export function base64ToBytes(base64: string): Uint8Array {
    return toByteArray(base64);
}

export function bytesToBase64(bytes: Uint8Array): string {
    return fromByteArray(bytes);
}

export function decodeImageBytes(bytes: Uint8Array): RgbaImage {
    if (isPng(bytes)) {
        const decoded = UPNG.decode(toExactArrayBuffer(bytes));
        const rgbaFrames = UPNG.toRGBA8(decoded);
        const rgba = new Uint8Array(rgbaFrames[0]);

        return {
            width: decoded.width,
            height: decoded.height,
            data: rgba,
        };
    }

    if (isJpeg(bytes)) {
        const decoded = jpeg.decode(bytes, { useTArray: true });

        return {
            width: decoded.width,
            height: decoded.height,
            data: decoded.data,
        };
    }

    throw new Error("Desteklenmeyen görsel formatı. PNG veya JPG seç.");
}

export function encodePngBase64(image: RgbaImage): string {
    const pngArrayBuffer = UPNG.encode(
        [toExactArrayBuffer(image.data)],
        image.width,
        image.height,
        0
    );

    return bytesToBase64(new Uint8Array(pngArrayBuffer));
}