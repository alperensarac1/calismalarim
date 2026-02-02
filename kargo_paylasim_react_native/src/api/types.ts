export type ApiResp<T> = {
    ok: boolean;
    data: T | null;
    error: string | null;
};

export type LoginData = { token: string; user_id: number };
export type RegisterData = { user_id: number; address_id: number };

export type Address = {
    id: number;
    title: string;
    city: string;
    district: string;
    address_line: string;
    is_default: number; // 1/0
};

export type AddressListData = { items: Address[] };

export type Shipment = {
    id: number;
    pickup_code: string;
    status: string;
    cargo_company_name?: string | null;
};

export type ShipmentListData = { items: Shipment[] };

export type LookupReceiverData = {
    receiver_user_id: number;
    masked_first_name: string;
    masked_last_name: string;
};

export type CreateShipmentData = {
    shipment_id: number;
    pickup_code: string;
    status: string;
    code_expires_at: string;
};
