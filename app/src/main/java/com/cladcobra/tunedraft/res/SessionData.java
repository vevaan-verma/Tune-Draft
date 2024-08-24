package com.cladcobra.tunedraft.res;

public class SessionData {

    private static int squadSize = 0;

    public static int getSquadSize() {
        return squadSize;
    }

    public static void setSquadSize(int squadSize) {
        SessionData.squadSize = squadSize;
    }

    public static void incrementSquadSize() {
        squadSize++;
    }

    public static void clearData() {
        squadSize = 0;
    }

}