import { http } from "./http";
import { Endpoints } from "./endpoints";
import { ApiResponse, CategoryDto, ProductDto, ProductListPage } from "./types";

export const productApi = {
    async getCategories() {
        const { data } = await http.get<ApiResponse<CategoryDto[]>>(Endpoints.categories);
        if (!data.ok || !data.data) throw new Error(data.error || "Categories failed");
        return data.data;
    },

    async getProducts(params: {
        cat?: number | null;
        q?: string | null;
        min?: number | null;
        max?: number | null;
        discount?: number | null; // 1/0
        sort?: string | null;
        page?: number | null;
        per?: number | null;
    }) {
        const { data } = await http.get<ApiResponse<ProductListPage>>(Endpoints.products, { params });
        if (!data.ok || !data.data) throw new Error(data.error || "Products failed");
        return data.data;
    },

    async getProduct(id: number) {
        const { data } = await http.get<ApiResponse<ProductDto>>(Endpoints.product(id));
        if (!data.ok || !data.data) throw new Error(data.error || "Product failed");
        return data.data;
    },
};
