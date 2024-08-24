package com.cladcobra.tunedraft.database;

import android.os.Handler;
import android.os.Looper;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cladcobra.tunedraft.database.listener.DoesTuneExistListener;
import com.cladcobra.tunedraft.database.listener.GetAllTunesListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Tune.class}, version = 1)
public abstract class TuneDatabase extends RoomDatabase {

    public abstract TuneDAO getTuneDAO();

    public void addTune(Tune tune) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getTuneDAO().addTune(tune));

    }

    public void removeTune(Tune tune) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getTuneDAO().removeTune(tune));

    }

    public void getAllTunes(GetAllTunesListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            List<Tune> tunes = getTuneDAO().getAllTunes(); // this line prevents main thread from being blocked
            handler.post(() -> listener.onGetAllTunes(tunes));

        });
    }

    public void doesTuneExist(Tune tune, DoesTuneExistListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            boolean exists = getTuneDAO().doesTuneExist(tune.getName(), tune.getArtist()); // this line prevents main thread from being blocked
            handler.post(() -> listener.onDoesTuneExist(exists));

        });
    }
}