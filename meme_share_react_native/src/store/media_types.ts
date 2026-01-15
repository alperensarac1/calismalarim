import {GonderiModel, UploadResponse} from "../service/meme_service";


export type MediaState = {
    isLoadingList: boolean;
    isUploading: boolean;
    error: string | null;

    posts: GonderiModel[];

    lastUpload: UploadResponse | null;
};

export type MediaAction =
    | { type: 'MEDIA_LIST_REQUEST' }
    | { type: 'MEDIA_LIST_SUCCESS'; payload: GonderiModel[] }
    | { type: 'MEDIA_LIST_FAIL'; payload: string }
    | { type: 'MEDIA_UPLOAD_REQUEST' }
    | { type: 'MEDIA_UPLOAD_SUCCESS'; payload: UploadResponse }
    | { type: 'MEDIA_UPLOAD_FAIL'; payload: string }
    | { type: 'MEDIA_CLEAR_ERROR' };
