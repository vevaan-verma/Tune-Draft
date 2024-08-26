package com.cladcobra.tunedraft.database;

import android.os.Handler;
import android.os.Looper;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cladcobra.tunedraft.listener.AllTunesRetrievalListener;
import com.cladcobra.tunedraft.listener.ContainsTuneListener;
import com.cladcobra.tunedraft.listener.DailyTuneRetrievalListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Tune.class, DailyTune.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TuneDAO getTuneDAO();

    public abstract DailyTuneDAO getDailyTuneDAO();

    public void addTune(Tune tune) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getTuneDAO().addTune(tune));

    }

    public void removeTune(Tune tune) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getTuneDAO().removeTune(tune));

    }

    public void getAllTunes(AllTunesRetrievalListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            List<Tune> tunes = getTuneDAO().getAllTunes(); // this line prevents main thread from being blocked
            handler.post(() -> listener.onAllTunesRetrieved(tunes));

        });
    }

    public void containsTune(Tune tune, ContainsTuneListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            boolean containsTune = getTuneDAO().containsTune(tune.getName(), tune.getArtist()); // this line prevents main thread from being blocked
            handler.post(() -> listener.onContainsTune(containsTune));

        });
    }

    public void getDailyTune(DailyTuneRetrievalListener listener) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {

            DailyTune dailyTune = getDailyTuneDAO().getDailyTune(); // this line prevents main thread from being blocked
            handler.post(() -> listener.onDailyTuneRetrieved(dailyTune));

        });
    }

    public void setDailyTune(DailyTune dailyTune) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> getDailyTuneDAO().setDailyTune(dailyTune));

    }
}