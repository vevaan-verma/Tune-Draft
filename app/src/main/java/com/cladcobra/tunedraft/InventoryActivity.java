package com.cladcobra.tunedraft;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cladcobra.tunedraft.database.Song;
import com.cladcobra.tunedraft.database.SongDatabase;

import org.jetbrains.annotations.NotNull;

public class InventoryActivity extends AppCompatActivity {

    private SongDatabase songDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

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
        songDatabase = Room.databaseBuilder(getApplicationContext(), SongDatabase.class, "song-database")
                .addCallback(callback)
                .build();

        createSongList();

    }

    private void createSongList() {

        LinearLayout inventoryLayout = findViewById(R.id.inventoryLayout);

        songDatabase.getAllSongs(songs -> {

            for (Song song : songs) {

                LinearLayout songInfoLayout = new LinearLayout(this);
                songInfoLayout.setOrientation(LinearLayout.VERTICAL);
                songInfoLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.inventory_song_info_bg));

                TextView songNameText = new TextView(this);
                songNameText.setText(String.format("%s", song.getName()));
                songNameText.setPadding(48, 0, 0, 0);
                songNameText.setTextSize(20);

                TextView artistNameText = new TextView(this);
                artistNameText.setText(String.format("%s", song.getArtist()));
                artistNameText.setPadding(48, 0, 0, 0);

                songInfoLayout.addView(songNameText);
                songInfoLayout.addView(artistNameText);

                inventoryLayout.addView(songInfoLayout);

                // add space between each song info element
                Space space = new Space(this);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64 // height of space in dp
                );
                space.setLayoutParams(spaceParams);
                inventoryLayout.addView(space);

            }
        });
    }
}