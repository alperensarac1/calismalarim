import {OdaModel, SimpleResponse} from "../service/meme_service";


export type RoomsState = {
    isLoadingRooms: boolean;
    isLoadingCreate: boolean;
    isLoadingJoin: boolean;
    error: string | null;

    rooms: OdaModel[];

    lastCreate: SimpleResponse | null;
    lastJoin: SimpleResponse | null;
};

export type RoomsAction =
    | { type: 'ROOMS_FETCH_REQUEST' }
    | { type: 'ROOMS_FETCH_SUCCESS'; payload: OdaModel[] }
    | { type: 'ROOMS_FETCH_FAIL'; payload: string }
    | { type: 'ROOMS_CREATE_REQUEST' }
    | { type: 'ROOMS_CREATE_SUCCESS'; payload: SimpleResponse }
    | { type: 'ROOMS_CREATE_FAIL'; payload: string }
    | { type: 'ROOMS_JOIN_REQUEST' }
    | { type: 'ROOMS_JOIN_SUCCESS'; payload: SimpleResponse }
    | { type: 'ROOMS_JOIN_FAIL'; payload: string };
