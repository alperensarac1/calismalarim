package com.example.onlineradyojava.data;

public class RadioRoom {

    private int id;
    private String roomName;
    private String currentMusic;
    private boolean playing;
    private int listenerCount;

    public RadioRoom(
            int id,
            String roomName,
            String currentMusic,
            boolean playing,
            int listenerCount
    ) {
        this.id = id;
        this.roomName = roomName;
        this.currentMusic = currentMusic;
        this.playing = playing;
        this.listenerCount = listenerCount;
    }

    public int getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getCurrentMusic() {
        return currentMusic;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getListenerCount() {
        return listenerCount;
    }
}