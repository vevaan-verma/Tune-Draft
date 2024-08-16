package com.cladcobra.tunedraft.database.listener;

import com.cladcobra.tunedraft.database.Song;

import java.util.List;

public interface GetAllSongsListener {

    void onGetAllSongs(List<Song> songs);

}