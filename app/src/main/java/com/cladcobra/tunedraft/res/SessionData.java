package com.cladcobra.tunedraft.res;

public class SessionData {

    private static int squadSongs = 0;

    public static int getSquadSongs() {
        return squadSongs;
    }

    public static void setSquadSongs(int squadSongs) {
        SessionData.squadSongs = squadSongs;
    }

    public static void incrementSquadSongs() {
        squadSongs++;
    }

    public static void clearData() {
        squadSongs = 0;
    }

}