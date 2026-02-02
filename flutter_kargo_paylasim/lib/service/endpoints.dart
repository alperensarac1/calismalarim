class Endpoints {
  static const base = "https://alperensaracdeneme.com/cargo/";

  static const login = "${base}user_login.php";
  static const register = "${base}user_register.php";
  static const me = "${base}user_me.php";

  static const addressList = "${base}address_list.php";
  static const addressCreate = "${base}address_create.php";
  static const addressUpdate = "${base}address_update.php";
  static const addressDelete = "${base}address_delete.php";
  static const addressSetDefault = "${base}address_set_default.php";

  static const receiverLookup = "${base}receiver_lookup.php";

  static const shipmentList = "${base}shipment_list.php"; // ?type=all|sent|received
  static const shipmentCreate = "${base}shipment_create.php";
  static const shipmentDetail = "${base}shipment_detail.php"; // (kullanacaksan)
}
