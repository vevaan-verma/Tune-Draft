package com.cladcobra.tunedraft.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tune")
public class Tune {

    @ColumnInfo(name = "tune_id")
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "tune_name")
    private String name;

    @ColumnInfo(name = "tune_artist")
    private String artist;

    @ColumnInfo(name = "tune_rank")
    private int rank;

    @Ignore
    public Tune() {

    }

    public Tune(String name, String artist, int rank) {

        this.id = 0;
        this.name = name;
        this.artist = artist;
        this.rank = rank;

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

}