import 'dart:collection';
import 'dart:math';

import 'package:image/image.dart' as img;

class ImageProcessor {
  static img.Image removeConnectedRegionByColor({
    required img.Image source,
    required int startX,
    required int startY,
    required double tolerance,
  }) {
    final result = img.Image.from(source);

    final width = result.width;
    final height = result.height;

    if (startX < 0 || startY < 0 || startX >= width || startY >= height) {
      return result;
    }

    final targetPixel = result.getPixel(startX, startY);
    final targetR = targetPixel.r.toInt();
    final targetG = targetPixel.g.toInt();
    final targetB = targetPixel.b.toInt();

    final visited = List<bool>.filled(width * height, false);
    final queue = Queue<_Point>()..add(_Point(startX, startY));

    while (queue.isNotEmpty) {
      final p = queue.removeFirst();
      final x = p.x;
      final y = p.y;

      if (x < 0 || y < 0 || x >= width || y >= height) continue;

      final index = y * width + x;
      if (visited[index]) continue;
      visited[index] = true;

      final pixel = result.getPixel(x, y);
      if (pixel.a.toInt() == 0) continue;

      final distance = _colorDistance(
        pixel.r.toInt(),
        pixel.g.toInt(),
        pixel.b.toInt(),
        targetR,
        targetG,
        targetB,
      );

      if (distance <= tolerance) {
        result.setPixelRgba(x, y, 0, 0, 0, 0);

        queue.add(_Point(x + 1, y));
        queue.add(_Point(x - 1, y));
        queue.add(_Point(x, y + 1));
        queue.add(_Point(x, y - 1));
      }
    }

    return result;
  }

  static double _colorDistance(
      int r1,
      int g1,
      int b1,
      int r2,
      int g2,
      int b2,
      ) {
    final dr = (r1 - r2).toDouble();
    final dg = (g1 - g2).toDouble();
    final db = (b1 - b2).toDouble();
    return sqrt(dr * dr + dg * dg + db * db);
  }
}

class _Point {
  final int x;
  final int y;

  _Point(this.x, this.y);
}