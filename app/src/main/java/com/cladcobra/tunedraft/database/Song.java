package com.cladcobra.tunedraft.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Song")
public class Song {

    @ColumnInfo(name = "song_id")
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "song_name")
    private String name;

    @ColumnInfo(name = "song_artist")
    private String artist;

    @Ignore
    public Song() {

    }

    public Song(String name, String artist) {

        this.id = 0;
        this.name = name;
        this.artist = artist;

    }

    public String getName() {

        return name;

    }

    public String getArtist() {

        return artist;

    }
}
