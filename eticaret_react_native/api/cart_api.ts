import { http } from "./http";
import { Endpoints } from "./endpoints";
import {
    ApiResponse,
    AddToCartRequest,
    AddToCartResponse,
    CartDto,
    UpdateCartItemRequest,
} from "./types";

export type BasicOk = { ok: boolean };

export const cartApi = {
    async getCart() {
        const { data } = await http.get<ApiResponse<CartDto>>(Endpoints.cart);
        if (!data.ok || !data.data) throw new Error(data.error || "Cart failed");
        return data.data;
    },

    async addToCart(body: AddToCartRequest) {
        const { data } = await http.post<ApiResponse<AddToCartResponse>>(Endpoints.cartAdd, body);
        if (!data.ok || !data.data) throw new Error(data.error || "Add to cart failed");
        return data.data;
    },

    async updateItem(itemId: number, body: UpdateCartItemRequest) {
        const { data } = await http.post<ApiResponse<BasicOk>>(Endpoints.cartItem, body, {
            params: { id: itemId },
        });
        if (!data.ok || !data.data) throw new Error(data.error || "Update item failed");
        return data.data.ok;
    },

    async deleteItem(itemId: number) {
        const { data } = await http.delete<ApiResponse<BasicOk>>(Endpoints.cartItem, {
            params: { id: itemId },
        });
        if (!data.ok || !data.data) throw new Error(data.error || "Delete item failed");
        return data.data.ok;
    },
};
