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

import com.cladcobra.tunedraft.database.SongDatabase;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        /* DATABASE INITIALIZATION */
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

        SongDatabase songDatabase = Room.databaseBuilder(getApplicationContext(), SongDatabase.class, "song-database")
                .addCallback(callback)
                .build();

        sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        Button clearDataButton = findViewById(R.id.clearDataButton);
        clearDataButton.setOnClickListener(v -> {

            sharedPrefs.edit().putInt(getString(R.string.drafts_remaining_key), 0).apply();
            this.deleteDatabase("song-database");

        });

        Button addDraftsButton = findViewById(R.id.addDraftsButton);
        addDraftsButton.setOnClickListener(v -> addTuneDraft());

        Button chartButton = findViewById(R.id.billboardHot100Button);
        chartButton.setOnClickListener(v -> loadChart());

        Button inventoryButton = findViewById(R.id.inventoryButton);
        inventoryButton.setOnClickListener(v -> {

            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);

        });
    }

    private void addTuneDraft() {

        int tunesRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);
        sharedPrefs.edit().putInt(getString(R.string.drafts_remaining_key), tunesRemaining + 1).apply();
        Log.d("debug-info", sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0) + "");

    }

    private void loadChart() {

        Intent intent = new Intent(this, Hot100Activity.class);
        startActivity(intent);

    }
}