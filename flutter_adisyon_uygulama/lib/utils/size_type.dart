enum SizeType {
  matchParent,
  wrapContent,
}

extension SizeTypeExt on SizeType {
  double? toSize() {
    switch (this) {
      case SizeType.matchParent:
        return double.infinity;
      case SizeType.wrapContent:
        return null;
    }
  }
}
