package com.cladcobra.tunedraft;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import com.cladcobra.tunedraft.chart.Hot100Chart;
import com.cladcobra.tunedraft.database.Song;
import com.cladcobra.tunedraft.database.SongDatabase;
import com.cladcobra.tunedraft.res.SessionData;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class Hot100Activity extends AppCompatActivity {

    // data storage
    private SongDatabase songDatabase;
    private SharedPreferences sharedPrefs;

    // UI elements
    private HashMap<Button, Song> draftButtons;
    private TextView draftsRemainingText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hot_100);

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

        draftButtons = new HashMap<>();
        sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        // set element variables
        draftsRemainingText = findViewById(R.id.draftsRemainingText);
        progressBar = findViewById(R.id.progressBar);

        // set element backgrounds
        draftsRemainingText.setBackground(AppCompatResources.getDrawable(this, R.drawable.drafts_remaining_text_bg));

        updateDraftsRemaining(); // update drafts remaining text
        createHot100Chart(); // create hot 100 chart

    }

    private void createHot100Chart() {

        Gson gson = new Gson();

        Runnable runnable = () -> {

            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            try {

                StringBuilder result = new StringBuilder();
                URL url = new URL("https://raw.githubusercontent.com/mhollingshead/billboard-hot-100/main/recent.json");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(100); // can be modified, if not set, connection never occurs
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d("debug-info", "Response Code: " + responseCode);

                if (responseCode == 200) {

                    Scanner scanner = new Scanner(url.openStream());

                    while (scanner.hasNext())
                        result.append(scanner.nextLine());

                    scanner.close();

                } else {

                    Log.d("debug-error", "Failed to fetch data. Response Code: " + responseCode);

                }

                Hot100Chart chart = gson.fromJson(result.toString(), Hot100Chart.class);

                runOnUiThread(() -> {

                    progressBar.post(() -> progressBar.setVisibility(View.GONE));
                    createSongButtons(chart);

                });
            } catch (MalformedURLException e) {

                Log.d("debug-error", "Malformed URL: " + e.getMessage());

            } catch (IOException e) {

                Log.d("debug-error", "IO Exception: " + e.getMessage());

            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void createSongButtons(Hot100Chart chart) {

        LinearLayout songListLayout = findViewById(R.id.songListLayout);
        int rank = 1;

        for (Hot100Chart.Hot100ChartData chartData : chart.getData()) {

            Song song = new Song(chartData.getSongName(), chartData.getArtist());

            // TODO: give layouts more descriptive names
            LinearLayout innerLayout = getInnerLayout(chartData); // inner linear vertical layout
            ConstraintLayout outerLayout = getOuterLayout(innerLayout, song, rank); // outer linear horizontal layout

            songListLayout.addView(outerLayout); // add outer layout to song list layout

            // add space between each song info element
            Space space = new Space(this);
            LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    64 // height of space in dp
            );
            space.setLayoutParams(spaceParams);
            songListLayout.addView(space);

            rank++; // increment rank

        }

        updateDraftButtonStates(); // update draft button states at the very end

    }

    /* RANK & SONG INFO CONTAINER */
    private ConstraintLayout getOuterLayout(LinearLayout innerLayout, Song song, int rank) {

        ConstraintLayout outerLayout = new ConstraintLayout(this);
        outerLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.chart_song_info_bg));

        // hot 100 rank text
        TextView hot100Rank = new TextView(this);
        hot100Rank.setText(String.format("%s", rank));
        hot100Rank.setPadding(48, 0, 0, 0);
        hot100Rank.setId(View.generateViewId()); // set id for constraint layout
        hot100Rank.setTextSize(30);

        // constrain inner layout to the right of hot 100 rank
        ConstraintLayout.LayoutParams innerLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        innerLayoutParams.startToEnd = hot100Rank.getId();
        innerLayout.setLayoutParams(innerLayoutParams);

        Button button = getDraftButton(song); // button to draft tune
        draftButtons.put(button, song); // add button & its song to hashmap of draft buttons

        outerLayout.addView(hot100Rank);
        outerLayout.addView(innerLayout); // add inner layout to outer layout
        outerLayout.addView(button);

        return outerLayout;

    }

    private Button getDraftButton(Song song) {

        // TODO: find more efficient way to store drafts remaining?

        Button button = new Button(this);
        button.setText(R.string.draft_tune_text);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.draft_button_bg));

        // constrain draft button to the right/end
        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        buttonParams.rightMargin = 48;
        button.setLayoutParams(buttonParams);

        button.setOnClickListener(v -> {

            int draftsRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);

            if (draftsRemaining > 0) { // make sure there are drafts remaining

                songDatabase.addSong(song); // add song to database

                sharedPrefs.edit().putInt(getString(R.string.drafts_remaining_key), draftsRemaining - 1).apply();
                SessionData.incrementSquadSongs(); // increment squad songs

                updateDraftButtonStates(); // update draft button states
                updateDraftsRemaining(); // update drafts remaining text

            }
        });

        return button;

    }

    /* SONG INFO */
    private LinearLayout getInnerLayout(Hot100Chart.Hot100ChartData chartData) {

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        // song name text
        TextView songNameText = new TextView(this);
        String song = chartData.getSongName();

        int maxSongChars = getResources().getInteger(R.integer.max_song_chars);

        if (song.length() > maxSongChars)
            song = song.substring(0, maxSongChars) + "...";

        songNameText.setText(song);
        songNameText.setPadding(48, 0, 0, 0);
        songNameText.setTextSize(20);

        // artist name text
        TextView artistNameText = new TextView(this);
        String artist = chartData.getArtist().replace("Featuring", "ft.");

        int maxArtistChars = getResources().getInteger(R.integer.max_artist_chars);

        if (artist.length() > maxArtistChars)
            artist = artist.substring(0, maxArtistChars) + "...";

        artistNameText.setText(artist);
        artistNameText.setPadding(48, 0, 0, 0);

        // add song and artist to layout
        innerLayout.addView(songNameText);
        innerLayout.addView(artistNameText);
        return innerLayout;

    }

    /* UI UTILS */
    private void updateDraftButtonStates() {

        int draftsRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);

        // disable all buttons if no tunes drafts remain or squad songs are full
        if (draftsRemaining == 0 || SessionData.getSquadSongs() >= getResources().getInteger(R.integer.max_squad_size))
            draftButtons.forEach(
                    (key, value) -> key.setEnabled(false)
            );

        // check if song already exists in database and disable button if it does
        draftButtons.forEach((button, song) -> {

            songDatabase.doesSongExist(song, songExists -> {

                if (songExists) button.setEnabled(false);

            });
        });

    }

    private void updateDraftsRemaining() {

        int draftsRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);
        draftsRemainingText.setText(String.format(getString(R.string.drafts_remaining_text) + " %d", draftsRemaining));

    }
}