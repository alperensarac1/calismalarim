import type { IntPoint, RgbaImage } from "./types";

function cloneImage(source: RgbaImage): RgbaImage {
    return {
        width: source.width,
        height: source.height,
        data: new Uint8Array(source.data),
    };
}

function colorDistance(
    r1: number,
    g1: number,
    b1: number,
    r2: number,
    g2: number,
    b2: number
): number {
    const dr = r1 - r2;
    const dg = g1 - g2;
    const db = b1 - b2;
    return Math.sqrt(dr * dr + dg * dg + db * db);
}

function pixelIndex(width: number, x: number, y: number): number {
    return (y * width + x) * 4;
}

export function removeConnectedRegionByColor(
    source: RgbaImage,
    startX: number,
    startY: number,
    tolerance: number
): RgbaImage {
    const result = cloneImage(source);
    const { width, height, data } = result;

    if (startX < 0 || startY < 0 || startX >= width || startY >= height) {
        return result;
    }

    const targetIdx = pixelIndex(width, startX, startY);
    const targetR = data[targetIdx];
    const targetG = data[targetIdx + 1];
    const targetB = data[targetIdx + 2];

    const visited = new Uint8Array(width * height);
    const queue: IntPoint[] = [{ x: startX, y: startY }];

    while (queue.length > 0) {
        const p = queue.shift()!;
        const x = p.x;
        const y = p.y;

        if (x < 0 || y < 0 || x >= width || y >= height) continue;

        const visitIndex = y * width + x;
        if (visited[visitIndex] === 1) continue;
        visited[visitIndex] = 1;

        const idx = pixelIndex(width, x, y);
        const r = data[idx];
        const g = data[idx + 1];
        const b = data[idx + 2];
        const a = data[idx + 3];

        if (a === 0) continue;

        const distance = colorDistance(r, g, b, targetR, targetG, targetB);

        if (distance <= tolerance) {
            data[idx] = 0;
            data[idx + 1] = 0;
            data[idx + 2] = 0;
            data[idx + 3] = 0;

            queue.push({ x: x + 1, y });
            queue.push({ x: x - 1, y });
            queue.push({ x, y: y + 1 });
            queue.push({ x, y: y - 1 });
        }
    }

    return result;
}
