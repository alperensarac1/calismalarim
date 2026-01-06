class Routes {
  static const login = '/login';
  static const register = '/register';

  // Tabs
  static const homeGundem = '/home/gundem';
  static const homeBugun  = '/home/bugun';
  static const homeProfil = '/home/profil';

  // Actions
  static const entryAdd = '/entry/add';
  static const entryDetail = '/entry/detail/:id';

  static String entryDetailPath(int id) => '/entry/detail/$id';
}
