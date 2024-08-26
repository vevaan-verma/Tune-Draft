package com.cladcobra.tunedraft.listener;

import com.cladcobra.tunedraft.database.DailyTune;

public interface DailyTuneRetrievalListener {
    void onDailyTuneRetrieved(DailyTune dailyTune);
}