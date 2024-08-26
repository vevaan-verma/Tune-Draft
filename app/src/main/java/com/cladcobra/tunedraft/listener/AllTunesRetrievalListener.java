package com.cladcobra.tunedraft.listener;

import com.cladcobra.tunedraft.database.Tune;

import java.util.List;

public interface AllTunesRetrievalListener {
    void onAllTunesRetrieved(List<Tune> tunes);
}