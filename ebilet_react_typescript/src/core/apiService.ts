import {parseTicket, type Ticket} from "../models/Ticket";
import { ApiClient } from "./apiClient";
import type {ApiResponse} from "../models/ApiResponse.ts";
import {parseUser, type User} from "../models/User.ts";
import {type City, parseCity} from "../models/City.ts";
import {type District, parseDistrict} from "../models/District.ts";
import {type AppEvent, parseEvent } from "../models/Event";

export class ApiService {
    static async register(params: {
        fullName: string;
        email: string;
        phone: string;
        password: string;
    }): Promise<ApiResponse<User>> {
        const json = await ApiClient.post<ApiResponse<any>>("auth/register.php", {
            full_name: params.fullName,
            email: params.email,
            phone: params.phone,
            password: params.password,
        });

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseUser(json.data) : null,
        };
    }

    static async login(params: {
        email: string;
        password: string;
    }): Promise<ApiResponse<User>> {
        const json = await ApiClient.post<ApiResponse<any>>("auth/login.php", {
            email: params.email,
            password: params.password,
        });

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseUser(json.data) : null,
        };
    }

    static async profile(apiToken: string): Promise<ApiResponse<User>> {
        const json = await ApiClient.post<ApiResponse<any>>("auth/profile.php", {
            api_token: apiToken,
        });

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseUser(json.data) : null,
        };
    }

    static async getCities(apiToken: string): Promise<ApiResponse<City[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            "locations/cities_list.php",
            {
                api_token: apiToken,
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseCity) : [],
        };
    }

    static async getDistrictsByCity(params: {
        apiToken: string;
        cityId: number;
    }): Promise<ApiResponse<District[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            "locations/districts_by_city.php",
            {
                api_token: params.apiToken,
                city_id: String(params.cityId),
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseDistrict) : [],
        };
    }


    static async getEventsByLocation(params: {
        apiToken: string;
        cityId: number;
        districtId: number;
    }): Promise<ApiResponse<AppEvent[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            "events/events_by_location.php",
            {
                api_token: params.apiToken,
                city_id: String(params.cityId),
                district_id: String(params.districtId),
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseEvent) : [],
        };
    }

    static async getEventDetail(params: {
        apiToken: string;
        eventId: number;
    }): Promise<ApiResponse<AppEvent>> {
        const json = await ApiClient.post<ApiResponse<any>>(
            "events/event_detail.php",
            {
                api_token: params.apiToken,
                event_id: String(params.eventId),
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseEvent(json.data) : null,
        };
    }

    static async buyTicket(params: {
        apiToken: string;
        eventId: number;
    }): Promise<ApiResponse<Ticket>> {
        const json = await ApiClient.post<ApiResponse<any>>(
            "tickets/ticket_buy.php",
            {
                api_token: params.apiToken,
                event_id: String(params.eventId),
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseTicket(json.data) : null,
        };
    }

    static async getMyTickets(apiToken: string): Promise<ApiResponse<Ticket[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            "tickets/my_tickets.php",
            {
                api_token: apiToken,
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseTicket) : [],
        };
    }

    static async getTicketDetail(params: {
        apiToken: string;
        ticketId: number;
    }): Promise<ApiResponse<Ticket>> {
        const json = await ApiClient.post<ApiResponse<any>>(
            "tickets/ticket_detail.php",
            {
                api_token: params.apiToken,
                ticket_id: String(params.ticketId),
            }
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseTicket(json.data) : null,
        };
    }
}