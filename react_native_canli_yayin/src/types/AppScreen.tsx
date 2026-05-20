import { RoomModel } from "../models/RoomModel";

export type AppScreen =
    | {
    name: "home";
}
    | {
    name: "room_list";
}
    | {
    name: "broadcaster";
}
    | {
    name: "viewer";
    room: RoomModel;
};