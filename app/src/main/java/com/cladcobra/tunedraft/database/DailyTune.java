package com.cladcobra.tunedraft.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// IMPORTANT: relies on the Tune class
@Entity(tableName = "DailyTune")
public class DailyTune {

    @ColumnInfo(name = "tune_id")
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "tune_name")
    String name;

    @ColumnInfo(name = "tune_artist")
    String artist;

    @ColumnInfo(name = "tune_rank")
    int rank;

    @ColumnInfo(name = "date")
    String date;

    @Ignore
    public DailyTune() {

    }

    public DailyTune(Tune dailyTune, String date) {

        this.name = dailyTune.getName();
        this.artist = dailyTune.getArtist();
        this.rank = dailyTune.getRank();
        this.date = date;

    }

    public DailyTune(String name, String artist, int rank, String date) {

        this.name = name;
        this.artist = artist;
        this.rank = rank;
        this.date = date;

    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public int getRank() {
        return rank;
    }

    public String getDate() {
        return date;
    }

}