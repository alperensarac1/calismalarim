class Endpoints {
  // sonunda / şart
  static const baseUrl = "https://alperensaracdeneme.com/eticaret/api/";


  static const login = "auth/login";
  static const register = "auth/register";

  // ProductApi
  static const categories = "categories";
  static const products = "products";
  static String product(int id) => "products/$id";

  // CartApi (php)
  static const cart = "cart.php";
  static const cartAdd = "cart_add.php";
  static const cartItem = "cart_item.php";

  // OrderApi (php)
  static const checkout = "checkout.php";
  static const orders = "orders.php";
  static const order = "order.php";
}
