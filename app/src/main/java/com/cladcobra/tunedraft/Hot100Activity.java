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
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cladcobra.tunedraft.chart.Hot100Chart;
import com.cladcobra.tunedraft.database.Tune;
import com.cladcobra.tunedraft.database.TuneDatabase;
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
    private TuneDatabase tuneDatabase;
    private SharedPreferences sharedPrefs;
    private HashMap<Button, Tune> draftButtons;

    // UI elements
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

        tuneDatabase = Room.databaseBuilder(getApplicationContext(), TuneDatabase.class, "tune-database")
                .addCallback(callback)
                .build();

        draftButtons = new HashMap<>();
        sharedPrefs = this.getSharedPreferences(getResources().getString(R.string.preference_file_key), MODE_PRIVATE);

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
                URL url = new URL(getResources().getString(R.string.data_url));

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
                    createTuneButtons(chart);

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

    private void createTuneButtons(Hot100Chart chart) {

        LinearLayout chartLayout = findViewById(R.id.chartLayout);
        int rank = 1;

        for (Hot100Chart.Hot100ChartData chartData : chart.getData()) {

            Tune tune = new Tune(chartData.getTuneName(), chartData.getArtistFormatted(), rank);

            LinearLayout tuneInfoLayout = getTuneInfoLayout(chartData); // inner tune info layout
            ConstraintLayout tuneElementLayout = getTuneElementLayout(tuneInfoLayout, tune, rank); // outer tune element layout

            chartLayout.addView(tuneElementLayout); // add tune element layout to tune list

            // add space between each tune info element
            Space space = new Space(this);
            LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    64 // height of space in dp
            );
            space.setLayoutParams(spaceParams);
            chartLayout.addView(space);

            rank++; // increment rank

        }

        updateDraftButtonStates(); // update draft button states at the very end

    }

    /* TUNE ELEMENT | RANK & TUNE INFO CONTAINER */
    private ConstraintLayout getTuneElementLayout(LinearLayout tuneInfoLayout, Tune tune, int rank) {

        ConstraintLayout tuneElementLayout = new ConstraintLayout(this);
        tuneElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.chart_tune_element_bg));

        // hot 100 rank text
        TextView hot100Rank = new TextView(this);
        hot100Rank.setText(String.format("#%s", rank));
        hot100Rank.setTypeface(ResourcesCompat.getFont(this, R.font.rem));
        hot100Rank.setTextColor(getResources().getColor(R.color.hot_100_rank_color, null));
        hot100Rank.setPadding(48, 0, 0, 0);
        hot100Rank.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement
        hot100Rank.setTextSize(30);

        // constrain tune info layout to the right of hot 100 rank
        ConstraintLayout.LayoutParams tuneInfoParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        tuneInfoParams.startToEnd = hot100Rank.getId();
        tuneInfoLayout.setLayoutParams(tuneInfoParams);

        Button draftButton = createDraftButton(tune); // button to draft tune
        draftButtons.put(draftButton, tune); // add button & its tune to hashmap of draft buttons

        tuneElementLayout.addView(hot100Rank);
        tuneElementLayout.addView(tuneInfoLayout); // add tune info layout to tune element layout
        tuneElementLayout.addView(draftButton);

        return tuneElementLayout;

    }

    private Button createDraftButton(Tune tune) {

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

            int draftsRemaining = sharedPrefs.getInt(getResources().getString(R.string.drafts_remaining_key), 0);

            if (draftsRemaining > 0) { // make sure there are drafts remaining

                tuneDatabase.addTune(tune); // add tune to database

                sharedPrefs.edit().putInt(getResources().getString(R.string.drafts_remaining_key), draftsRemaining - 1).apply();
                SessionData.incrementSquadSize(); // increment squad tunes

                updateDraftButtonStates(); // update draft button states
                updateDraftsRemaining(); // update drafts remaining text

            }
        });

        return button;

    }

    /* TUNE INFO */
    private LinearLayout getTuneInfoLayout(Hot100Chart.Hot100ChartData chartData) {

        LinearLayout tuneInfoLayout = new LinearLayout(this);
        tuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

        // TODO: deal with tune #100 character limit

        // tune name text
        TextView tuneNameText = new TextView(this);
        String tuneName = chartData.getTuneName();

        // clamp tune name to max length
        int maxTuneChars = getResources().getInteger(R.integer.max_tune_chars);

        if (tuneName.length() > maxTuneChars)
            tuneName = tuneName.substring(0, maxTuneChars) + "...";

        tuneNameText.setText(tuneName);
        tuneNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
        tuneNameText.setPadding(48, 0, 0, 0);
        tuneNameText.setTextSize(24);

        // artist name text
        TextView artistNameText = new TextView(this);
        String artistName = chartData.getArtistFormatted();

        // clamp artist name to max length
        int maxArtistChars = getResources().getInteger(R.integer.max_artist_chars);

        if (artistName.length() > maxArtistChars)
            artistName = artistName.substring(0, maxArtistChars) + "...";

        artistNameText.setText(artistName);
        artistNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
        artistNameText.setTextSize(16);
        artistNameText.setPadding(48, 0, 0, 0);

        // add tune and artist to layout
        tuneInfoLayout.addView(tuneNameText);
        tuneInfoLayout.addView(artistNameText);
        return tuneInfoLayout;

    }

    /* UI UTILS */
    private void updateDraftButtonStates() {

        int draftsRemaining = sharedPrefs.getInt(getResources().getString(R.string.drafts_remaining_key), 0);

        // disable all buttons if no tunes drafts remain or squad is full
        if (draftsRemaining == 0 || SessionData.getSquadSize() >= getResources().getInteger(R.integer.max_squad_size))
            draftButtons.forEach(
                    (key, value) -> key.setEnabled(false)
            );

        // check if tune already exists in database and disable button if it does
        draftButtons.forEach((button, tune) -> tuneDatabase.doesTuneExist(tune, tuneExists -> {
            if (tuneExists) button.setEnabled(false);
        }));

    }

    private void updateDraftsRemaining() {

        int draftsRemaining = sharedPrefs.getInt(getResources().getString(R.string.drafts_remaining_key), 0);
        draftsRemainingText.setText(String.format(getResources().getString(R.string.drafts_remaining_text) + " %d", draftsRemaining));

    }

}