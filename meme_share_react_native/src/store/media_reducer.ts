import {MediaAction, MediaState} from "./media_types";


export const initialMediaState: MediaState = {
    isLoadingList: false,
    isUploading: false,
    error: null,
    posts: [],
    lastUpload: null,
};

export function mediaReducer(state: MediaState, action: MediaAction): MediaState {
    switch (action.type) {
        case 'MEDIA_LIST_REQUEST':
            return { ...state, isLoadingList: true, error: null };
        case 'MEDIA_LIST_SUCCESS':
            return { ...state, isLoadingList: false, posts: action.payload };
        case 'MEDIA_LIST_FAIL':
            return { ...state, isLoadingList: false, error: action.payload };

        case 'MEDIA_UPLOAD_REQUEST':
            return { ...state, isUploading: true, error: null, lastUpload: null };
        case 'MEDIA_UPLOAD_SUCCESS':
            return { ...state, isUploading: false, lastUpload: action.payload };
        case 'MEDIA_UPLOAD_FAIL':
            return { ...state, isUploading: false, error: action.payload };

        case 'MEDIA_CLEAR_ERROR':
            return { ...state, error: null };

        default:
            return state;
    }
}
