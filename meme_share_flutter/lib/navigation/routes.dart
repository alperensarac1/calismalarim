class Routes {
  static const login = '/login';
  static const register = '/register';
  static const home = '/home';
  static const oda = '/oda';

  static String homePath(int userId) => '$home/$userId';
  static String odaPath(int roomId, int userId) => '$oda/$roomId/$userId';
}
