package com.cladcobra.tunedraft.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDAO {

    @Insert
    void addSong(Song song);

    @Update
    void updateSong(Song song);

    @Delete
    void deleteSong(Song song);

    @Query("select * from song")
    List<Song> getAllSongs();

    @Query("select * from song where song_id == :song_id")
    Song getSong(int song_id);

    @Query("SELECT COUNT(*) > 0 FROM song WHERE song_name = :name AND song_artist = :artist")
    boolean isSongExists(String name, String artist);

}
