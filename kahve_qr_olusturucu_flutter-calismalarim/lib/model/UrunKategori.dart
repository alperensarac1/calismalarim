enum UrunKategori {
  INDIRIMLI,
  ATISTIRMALIKLAR,
  ICECEKLER,
}

extension UrunKategoriExtension on UrunKategori {
  int get kategoriKodu {
    switch (this) {
      case UrunKategori.ICECEKLER:
        return 1;
      case UrunKategori.ATISTIRMALIKLAR:
        return 2;
      case UrunKategori.INDIRIMLI:
        return 0;
    }
  }
}

