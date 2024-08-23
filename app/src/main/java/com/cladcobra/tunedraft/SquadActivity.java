package com.cladcobra.tunedraft;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cladcobra.tunedraft.database.Song;
import com.cladcobra.tunedraft.database.SongDatabase;
import com.cladcobra.tunedraft.util.SquadElementLayout;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SquadActivity extends AppCompatActivity {

    // data storage
    private SongDatabase songDatabase;
    private HashMap<Button, SquadElementLayout> releaseButtonElements;

    // UI elements
    private LinearLayout squadLayout;
    private TextView squadText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_squad);

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

        releaseButtonElements = new HashMap<>();

        // set element variables
        squadText = findViewById(R.id.squadText);

        // set element backgrounds
        squadText.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_text_bg));

        createSongList();

    }

    private void createSongList() {

        squadLayout = findViewById(R.id.squadLayout);

        songDatabase.getAllSongs(songs -> {

            for (Song song : songs) {

                // space between each song info element
                Space space = new Space(this);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64 // height of space in dp
                );
                space.setLayoutParams(spaceParams);

                ConstraintLayout squadElementLayout = new ConstraintLayout(this);
                squadElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_song_element_bg));

                LinearLayout songInfoLayout = new LinearLayout(this);
                songInfoLayout.setOrientation(LinearLayout.VERTICAL);

                TextView songNameText = new TextView(this);
                songNameText.setText(String.format("%s", song.getName()));
                songNameText.setPadding(48, 0, 0, 0);
                songNameText.setTextSize(20);

                TextView artistNameText = new TextView(this);
                artistNameText.setText(String.format("%s", song.getArtist()));
                artistNameText.setPadding(48, 0, 0, 0);

                Button releaseButton = createReleaseButton(song);
                releaseButtonElements.put(releaseButton, new SquadElementLayout(squadElementLayout, space)); // store layout and space for removal

                songInfoLayout.addView(songNameText);
                songInfoLayout.addView(artistNameText);

                squadElementLayout.addView(songInfoLayout);
                squadElementLayout.addView(releaseButton);

                squadLayout.addView(squadElementLayout);

                squadLayout.addView(space);

            }
        });
    }

    private Button createReleaseButton(Song song) {

        // TODO: find more efficient way to store drafts remaining?

        Button button = new Button(this);
        button.setText(R.string.release_tune_text);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.release_button_bg));

        // constrain draft button to the right/end
        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        buttonParams.rightMargin = 48;
        button.setLayoutParams(buttonParams);

        button.setOnClickListener(v -> {

            songDatabase.removeSong(song); // remove song from database

            SquadElementLayout elementLayout = releaseButtonElements.get(button);

            if (elementLayout != null) {

                // remove squad element layout and space
                squadLayout.removeView(elementLayout.getLayout());
                squadLayout.removeView(elementLayout.getSpace());

            }
        });

        return button;

    }
}