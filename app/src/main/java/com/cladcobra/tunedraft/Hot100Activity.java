package com.cladcobra.tunedraft;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
                    createTuneList(chart);

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

    private void createTuneList(Hot100Chart chart) {

        LinearLayout tuneList = findViewById(R.id.tuneList);
        int rank = 1;

        for (Hot100Chart.Hot100ChartData chartData : chart.getData()) {

            // creation order: rank text -> draft button -> tune info layout -> tune element layout -> space
            // layout order: rank text -> tune info layout -> draft button
            // CONVENTION: create the most nested elements first | create the elements on the side first, then create the middle element and constrain it to the side elements

            Tune tune = new Tune(chartData.getTuneName(), chartData.getArtistFormatted(), rank);

            // region RANK TEXT
            TextView rankText = new TextView(this);
            rankText.setText(String.format("#%s", rank));
            rankText.setTypeface(ResourcesCompat.getFont(this, R.font.rem));
            rankText.setTextColor(getResources().getColor(R.color.hot_100_rank_color, null));
            rankText.setTextSize(30);
            rankText.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement

            // region RANK TEXT CONSTRAINTS
            // constrain rank text to: top, bottom, start of parent
            ConstraintLayout.LayoutParams rankParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            rankParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            rankParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            rankParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            rankParams.leftMargin = 48;
            rankText.setLayoutParams(rankParams);
            // endregion
            // endregion

            // region DRAFT BUTTON
            Button draftButton = new Button(this);
            draftButton.setText(R.string.draft_tune_text);
            draftButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.draft_button_bg));
            draftButton.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement

            // constrain draft button to: top, bottom, end of parent
            ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            buttonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            buttonParams.rightMargin = 48;
            draftButton.setLayoutParams(buttonParams);

            draftButton.setOnClickListener(v -> {

                int draftsRemaining = sharedPrefs.getInt(getResources().getString(R.string.drafts_remaining_key), 0);

                if (draftsRemaining > 0) { // make sure there are drafts remaining

                    tuneDatabase.addTune(tune); // add tune to database

                    sharedPrefs.edit().putInt(getResources().getString(R.string.drafts_remaining_key), draftsRemaining - 1).apply();
                    SessionData.incrementSquadSize(); // increment squad tunes

                    updateDraftButtonStates(); // update draft button states
                    updateDraftsRemaining(); // update drafts remaining text

                }
            });
            // endregion

            // region TUNE INFO LAYOUT (TUNE NAME & ARTIST NAME)
            LinearLayout tuneInfoLayout = new LinearLayout(this);
            tuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

            // region TUNE NAME TEXT
            TextView tuneNameText = new TextView(this);
            String tuneName = chartData.getTuneName();

            tuneNameText.setText(tuneName);
            tuneNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
            tuneNameText.setTextSize(24);
            tuneNameText.setEllipsize(TextUtils.TruncateAt.END);
            tuneNameText.setSingleLine();
            // endregion

            // region ARTIST NAME TEXT
            TextView artistNameText = new TextView(this);
            String artistName = chartData.getArtistFormatted();

            artistNameText.setText(artistName);
            artistNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
            artistNameText.setTextSize(16);
            artistNameText.setEllipsize(TextUtils.TruncateAt.END);
            artistNameText.setSingleLine();
            // endregion

            // add tune and artist texts to layout
            tuneInfoLayout.addView(tuneNameText);
            tuneInfoLayout.addView(artistNameText);

            // region TUNE INFO LAYOUT CONSTRAINTS
            // constrain tune info layout to: top, bottom of parent | end of hot 100 rank | start of draft button
            ConstraintLayout.LayoutParams tuneInfoParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            tuneInfoParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            tuneInfoParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            tuneInfoParams.startToEnd = rankText.getId();
            tuneInfoParams.endToStart = draftButton.getId();
            tuneInfoParams.leftMargin = 48;
            tuneInfoParams.rightMargin = 48;
            tuneInfoLayout.setLayoutParams(tuneInfoParams);
            // endregion
            // endregion

            // region TUNE ELEMENT LAYOUT
            ConstraintLayout tuneElementLayout = new ConstraintLayout(this);
            tuneElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.chart_tune_element_bg));

            // add rank text, tune info layout, and draft button to tune element layout
            tuneElementLayout.addView(rankText);
            tuneElementLayout.addView(tuneInfoLayout);
            tuneElementLayout.addView(draftButton);

            tuneList.addView(tuneElementLayout); // add tune element layout to tune list
            // endregion

            // region SPACE
            Space space = new Space(this);
            LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    64 // height of space in dp
            );
            space.setLayoutParams(spaceParams);

            tuneList.addView(space); // add space to tune list
            // endregion

            draftButtons.put(draftButton, tune); // add button & its tune to hashmap of draft buttons
            rank++; // increment rank

        }

        updateDraftButtonStates(); // update draft button states at the very end

    }

    // TODO: find more efficient way to store drafts remaining?
    // TODO: update disabled button visuals

    // region UI UTILITIES
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
    // endregion

}