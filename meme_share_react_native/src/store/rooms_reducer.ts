import {RoomsAction, RoomsState} from "./rooms_types";


export const initialRoomsState: RoomsState = {
    isLoadingRooms: false,
    isLoadingCreate: false,
    isLoadingJoin: false,
    error: null,
    rooms: [],
    lastCreate: null,
    lastJoin: null,
};

export function roomsReducer(state: RoomsState, action: RoomsAction): RoomsState {
    switch (action.type) {
        case 'ROOMS_FETCH_REQUEST':
            return { ...state, isLoadingRooms: true, error: null };
        case 'ROOMS_FETCH_SUCCESS':
            return { ...state, isLoadingRooms: false, rooms: action.payload };
        case 'ROOMS_FETCH_FAIL':
            return { ...state, isLoadingRooms: false, error: action.payload };

        case 'ROOMS_CREATE_REQUEST':
            return { ...state, isLoadingCreate: true, error: null };
        case 'ROOMS_CREATE_SUCCESS':
            return { ...state, isLoadingCreate: false, lastCreate: action.payload };
        case 'ROOMS_CREATE_FAIL':
            return { ...state, isLoadingCreate: false, error: action.payload };

        case 'ROOMS_JOIN_REQUEST':
            return { ...state, isLoadingJoin: true, error: null };
        case 'ROOMS_JOIN_SUCCESS':
            return { ...state, isLoadingJoin: false, lastJoin: action.payload };
        case 'ROOMS_JOIN_FAIL':
            return { ...state, isLoadingJoin: false, error: action.payload };

        default:
            return state;
    }
}
