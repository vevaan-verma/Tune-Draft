package com.cladcobra.tunedraft;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cladcobra.tunedraft.database.TuneDatabase;
import com.cladcobra.tunedraft.res.SessionData;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // region BOILERPLATE
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // endregion

        // region DATA STORAGE INITIALIZATION
        RoomDatabase.Callback callback = new RoomDatabase.Callback() {

            @Override
            public void onCreate(@NotNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onOpen(@NotNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }

        };

        TuneDatabase tuneDatabase = Room.databaseBuilder(getApplicationContext(), TuneDatabase.class, "tune-database")
                .addCallback(callback)
                .build();

        sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        // endregion

        // initialize session data from data storage
        tuneDatabase.getAllTunes(tunes -> SessionData.setSquadSize(tunes.size())); // initialize squad size from database
        SessionData.setTuneDrafts(sharedPrefs.getInt(getResources().getString(R.string.tune_drafts_key), 0)); // initialize tune drafts from shared preferences

        // region BUTTONS
        Button clearDataButton = findViewById(R.id.clearDataButton);
        clearDataButton.setOnClickListener(v -> clearData());

        Button addDraftsButton = findViewById(R.id.addDraftsButton);
        addDraftsButton.setOnClickListener(v -> addTuneDraft());

        Button chartButton = findViewById(R.id.billboardHot100Button);
        chartButton.setOnClickListener(v -> loadChart());

        Button inventoryButton = findViewById(R.id.inventoryButton);
        inventoryButton.setOnClickListener(v -> {

            Intent intent = new Intent(this, SquadActivity.class);
            startActivity(intent);

        });
        // endregion

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        sharedPrefs.edit().putInt(getResources().getString(R.string.tune_drafts_key), SessionData.getTuneDrafts()).apply(); // save tune drafts to shared preferences when activity is destroyed

    }

    private void clearData() {

        sharedPrefs.edit().clear().apply();
        this.deleteDatabase("tune-database");
        SessionData.clearData();

    }

    private void addTuneDraft() {

        SessionData.incrementTuneDrafts();
        Log.d("debug-info", "Tune Drafts: " + SessionData.getTuneDrafts());

    }

    private void loadChart() {

        Intent intent = new Intent(this, Hot100Activity.class);
        startActivity(intent);

    }

}