package com.cladcobra.tunedraft.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface DailyTuneDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setDailyTune(DailyTune dailyTune);

    @Query("select * from DailyTune limit 1")
    DailyTune getDailyTune();

}