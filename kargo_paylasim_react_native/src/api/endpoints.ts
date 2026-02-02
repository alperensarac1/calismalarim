export const Endpoints = {
    base: "https://alperensaracdeneme.com/cargo/",

    login: "user_login.php",
    register: "user_register.php",
    me: "user_me.php",

    addressList: "address_list.php",
    addressCreate: "address_create.php",
    addressUpdate: "address_update.php",
    addressDelete: "address_delete.php",
    addressSetDefault: "address_set_default.php",

    receiverLookup: "receiver_lookup.php",

    shipmentList: "shipment_list.php", // ?type=all|sent|received
    shipmentCreate: "shipment_create.php",
    shipmentDetail: "shipment_detail.php",
} as const;
