package com.cladcobra.tunedraft.database;

import android.os.Handler;
import android.os.Looper;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cladcobra.tunedraft.database.listener.DoesSongExistListener;
import com.cladcobra.tunedraft.database.listener.GetAllSongsListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Song.class}, version = 1)
public abstract class SongDatabase extends RoomDatabase {

    public abstract SongDAO getSongDAO();

    public void addSong(Song song) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getSongDAO().addSong(song));

    }

    public void getAllSongs(GetAllSongsListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            List<Song> songs = getSongDAO().getAllSongs(); // this line prevents main thread from being blocked
            handler.post(() -> listener.onGetAllSongs(songs));

        });
    }

    public void doesSongExist(Song song, DoesSongExistListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            boolean exists = getSongDAO().isSongExists(song.getName(), song.getArtist()); // this line prevents main thread from being blocked
            handler.post(() -> listener.onDoesSongExist(exists));

        });
    }
}