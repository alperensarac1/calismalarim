import {ApiResponse} from '../models/ApiResponse';
import {City, parseCity} from '../models/City';
import {District, parseDistrict} from '../models/District';
import {Event, parseEvent} from '../models/Event';
import {Ticket, parseTicket} from '../models/Ticket';
import {User, parseUser} from '../models/User';
import {Venue, parseVenue} from '../models/Venue';
import {ApiClient} from './apiClient';

export class ApiService {
    static async register(params: {
        fullName: string;
        email: string;
        phone: string;
        password: string;
    }): Promise<ApiResponse<User>> {
        const json = await ApiClient.post<ApiResponse<any>>('auth/register.php', {
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
        const json = await ApiClient.post<ApiResponse<any>>('auth/login.php', {
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
        const json = await ApiClient.post<ApiResponse<any>>('auth/profile.php', {
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
            'locations/cities_list.php',
            {
                api_token: apiToken,
            },
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
            'locations/districts_by_city.php',
            {
                api_token: params.apiToken,
                city_id: String(params.cityId),
            },
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseDistrict) : [],
        };
    }

    static async getVenuesByDistrict(params: {
        apiToken: string;
        cityId: number;
        districtId: number;
    }): Promise<ApiResponse<Venue[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            'locations/venues_by_district.php',
            {
                api_token: params.apiToken,
                city_id: String(params.cityId),
                district_id: String(params.districtId),
            },
        );

        return {
            success: json.success,
            message: json.message,
            data: Array.isArray(json.data) ? json.data.map(parseVenue) : [],
        };
    }

    static async getEventsByLocation(params: {
        apiToken: string;
        cityId: number;
        districtId: number;
    }): Promise<ApiResponse<Event[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            'events/events_by_location.php',
            {
                api_token: params.apiToken,
                city_id: String(params.cityId),
                district_id: String(params.districtId),
            },
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
    }): Promise<ApiResponse<Event>> {
        const json = await ApiClient.post<ApiResponse<any>>(
            'events/event_detail.php',
            {
                api_token: params.apiToken,
                event_id: String(params.eventId),
            },
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
            'tickets/ticket_buy.php',
            {
                api_token: params.apiToken,
                event_id: String(params.eventId),
            },
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseTicket(json.data) : null,
        };
    }

    static async getMyTickets(apiToken: string): Promise<ApiResponse<Ticket[]>> {
        const json = await ApiClient.post<ApiResponse<any[]>>(
            'tickets/my_tickets.php',
            {
                api_token: apiToken,
            },
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
            'tickets/ticket_detail.php',
            {
                api_token: params.apiToken,
                ticket_id: String(params.ticketId),
            },
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseTicket(json.data) : null,
        };
    }

    static async checkTicket(params: {
        apiToken: string;
        ticketCode: string;
    }): Promise<ApiResponse<Ticket>> {
        const json = await ApiClient.post<ApiResponse<any>>(
            'check/ticket_check.php',
            {
                api_token: params.apiToken,
                ticket_code: params.ticketCode,
            },
        );

        return {
            success: json.success,
            message: json.message,
            data: json.data ? parseTicket(json.data) : null,
        };
    }
}