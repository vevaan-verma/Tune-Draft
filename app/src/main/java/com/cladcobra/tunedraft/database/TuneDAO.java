package com.cladcobra.tunedraft.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TuneDAO {

    @Insert
    void addTune(Tune tune);

    @Delete
    void removeTune(Tune tune);

    @Query("select * from tune")
    List<Tune> getAllTunes();

    @Query("select COUNT(*) > 0 from tune where tune_name = :name and tune_artist = :artist")
    boolean containsTune(String name, String artist); // don't take rank into account because a user cannot have the same song with different ranks

}