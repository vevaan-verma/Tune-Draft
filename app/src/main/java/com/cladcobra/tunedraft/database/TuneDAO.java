package com.cladcobra.tunedraft.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TuneDAO {

    @Insert
    void addTune(Tune tune);

    @Update
    void updateTune(Tune tune);

    @Delete
    void removeTune(Tune tune);

    @Query("select * from tune")
    List<Tune> getAllTunes();

    @Query("select * from tune where tune_id == :id")
    Tune getTune(int id);

    @Query("SELECT COUNT(*) > 0 FROM tune WHERE tune_name = :name AND tune_artist = :artist")
    boolean doesTuneExist(String name, String artist);

}