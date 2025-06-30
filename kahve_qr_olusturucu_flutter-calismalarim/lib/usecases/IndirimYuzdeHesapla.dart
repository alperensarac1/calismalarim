String indirimYuzdeHesapla(double indirimsizFiyat, double indirimliFiyat) {
  if (indirimsizFiyat <= 0) return "0.0";

  double indirimOrani = ((indirimsizFiyat - indirimliFiyat) / indirimsizFiyat) * 100;
  return indirimOrani.toStringAsFixed(1);
}
