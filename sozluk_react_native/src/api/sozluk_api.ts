import { SimpleResponse } from "../models/simple_response";
import { api } from "./client";
import {Entry} from "../models/entry";
import {VoteRequest} from "../models/vote_response";


export const SozlukApi = {
    registerUser: async (body: { username: string; password: string; email: string }) => {
        const { data } = await api.post<SimpleResponse>("sozluk_register.php", body);
        return data;
    },

    loginUser: async (body: { username: string; password: string }) => {
        const { data } = await api.post<SimpleResponse>("sozluk_login.php", body);
        return data;
    },

    addEntry: async (body: { user_id: string; title: string; content: string }) => {
        const { data } = await api.post<SimpleResponse>("sozluk_entry_insert.php", body);
        return data;
    },

    getAllEntries: async () => {
        const { data } = await api.get<Entry[]>("sozluk_entry_list.php");
        return data;
    },

    getEntriesByUser: async (user_id: number) => {
        const { data } = await api.get<Entry[]>("sozluk_entry_by_user.php", { params: { user_id } });
        return data;
    },

    addComment: async (body: { entry_id: string; user_id: string; comment_text: string }) => {
        const { data } = await api.post<SimpleResponse>("sozluk_comment_insert.php", body);
        return data;
    },

    getCommentsByEntry: async (entry_id: number) => {
        const { data } = await api.get<Comment[]>("sozluk_comments_by_entry.php", { params: { entry_id } });
        return data;
    },

    voteComment: async (req: VoteRequest) => {
        const { data } = await api.post<SimpleResponse>("sozluk_like_comment.php", req);
        return data;
    },

    deleteEntry: async (body: { entry_id: number }) => {
        const { data } = await api.post<SimpleResponse>("sozluk_entry_delete.php", body);
        return data;
    },

    getEntryById: async (entry_id: number) => {
        const { data } = await api.get<Entry | null>("sozluk_entry_get.php", { params: { entry_id } });
        return data;
    },
};
