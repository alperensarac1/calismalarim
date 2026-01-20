import { http } from "./http";
import { Endpoints } from "./endpoints";
import { ApiResponse, OrderDetailDto, OrderSummaryDto } from "./types";

export type CheckoutRequest = {
    address_name: string;
    address_line1: string;
    address_line2?: string | null;
    city: string;
    district: string;
    postal_code: string;
};

export type CheckoutResponse = { order_id: number };

export const orderApi = {
    async checkout(body: CheckoutRequest) {
        const { data } = await http.post<ApiResponse<CheckoutResponse>>(Endpoints.checkout, body);
        if (!data.ok || !data.data) throw new Error(data.error || "Checkout failed");
        return data.data;
    },

    async getOrders() {
        const { data } = await http.get<ApiResponse<OrderSummaryDto[]>>(Endpoints.orders);
        if (!data.ok || !data.data) throw new Error(data.error || "Orders failed");
        return data.data;
    },

    async getOrderDetail(orderId: number) {
        const { data } = await http.get<ApiResponse<OrderDetailDto>>(Endpoints.order, { params: { id: orderId } });
        if (!data.ok || !data.data) throw new Error(data.error || "Order detail failed");
        return data.data;
    },
};
