export const Endpoints = {
    baseUrl: "https://alperensaracdeneme.com/eticaret/api/",

    // Auth
    login: "auth/login",
    register: "auth/register",
    me: "me",

    // Products
    categories: "categories",
    products: "products",
    product: (id: number) => `products/${id}`,

    // Cart (php)
    cart: "cart.php",
    cartAdd: "cart_add.php",
    cartItem: "cart_item.php",

    // Orders (php)
    checkout: "checkout.php",
    orders: "orders.php",
    order: "order.php",
} as const;
