export type ApiResponse<T> = {
    ok: boolean;
    data?: T;
    error?: string;
};

export type CategoryDto = {
    id: number;
    name: string;
};

export type ProductListDto = {
    id: number;
    name: string;
    price: number;
    discount_percent?: number | null;
    image_url?: string | null;
    stock_qty: number;
    is_active: number;
};

export type ProductListPage = {
    items: ProductListDto[];
    total: number;
    page: number;
    per: number;
};

export type ProductDto = {
    id: number;
    name: string;
    slug: string;
    sku: string;
    price: number;
    discount_percent?: number | null;
    image_url?: string | null;
    stock_qty: number;
    is_active: number;
    created_at: string;
    updated_at?: string | null;
};

export type CartDto = {
    cart_id?: number | null;
    items: CartItemDto[];
    total: number;
    total_items: number;
};

export type CartItemDto = {
    item_id: number;
    quantity: number;
    product_id: number;
    name: string;
    sku?: string | null;
    image_url?: string | null;
    stock_qty: number;
    price: number;
    discount_percent?: number | null;
    sale_price: number;
};

export type AddToCartRequest = {
    product_id: number;
    quantity: number;
};

export type AddToCartResponse = {
    cart_id: number;
    item_id: number;
    quantity: number;
};

export type UpdateCartItemRequest = {
    quantity: number;
};

export type OrderSummaryDto = {
    id: number;
    status: string;
    total_amount: number;
    currency: string;
    created_at: string;
};

export type OrderItemDto = {
    product_id: number;
    name: string;
    sku?: string | null;
    unit_price: number;
    quantity: number;
    line_total: number;
};

export type PaymentDto = {
    [key: string]: any;
};

export type OrderDetailDto = {
    id: number;
    status: string;
    total_amount: number;
    currency: string;

    address_name?: string | null;
    address_line1?: string | null;
    address_line2?: string | null;
    city?: string | null;
    district?: string | null;
    postal_code?: string | null;

    created_at: string;
    items: OrderItemDto[];
    payment?: PaymentDto | null;
};
