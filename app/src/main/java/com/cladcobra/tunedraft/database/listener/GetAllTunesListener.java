package com.cladcobra.tunedraft.database.listener;

import com.cladcobra.tunedraft.database.Tune;

import java.util.List;

public interface GetAllTunesListener {

    void onGetAllTunes(List<Tune> tunes);

}