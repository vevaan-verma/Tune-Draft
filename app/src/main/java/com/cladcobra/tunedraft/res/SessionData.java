package com.cladcobra.tunedraft.res;

public class SessionData {

    private static int tuneDrafts; // saved in shared preferences
    private static int squadSize; // set from database values (no need to be stored)

    public static int getTuneDrafts() {
        return tuneDrafts;
    }

    public static void setTuneDrafts(int tuneDrafts) {
        SessionData.tuneDrafts = tuneDrafts;
    }

    public static void incrementTuneDrafts() {
        tuneDrafts++;
    }

    public static void decrementTuneDrafts() {
        tuneDrafts--;
    }

    public static int getSquadSize() {
        return squadSize;
    }

    public static void setSquadSize(int squadSize) {
        SessionData.squadSize = squadSize;
    }

    public static void incrementSquadSize() {
        squadSize++;
    }

    public static void decrementSquadSize() {
        squadSize--;
    }

    public static void clearData() {

        tuneDrafts = 0;
        squadSize = 0;

    }

}