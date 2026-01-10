import {HaberModel} from "../model/haber_model";

export type RootStackParamList = {
    Home: undefined;
    Detail: { haber: HaberModel };
    Category: { kategoriId: number; kategoriAdi?: string };
};
