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
import com.cladcobra.tunedraft.database.AppDatabase;
import com.cladcobra.tunedraft.database.Tune;
import com.cladcobra.tunedraft.listener.ChartRetrievalListener;
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
    private AppDatabase appDatabase;
    private SharedPreferences sharedPrefs;
    private HashMap<Button, Tune> draftButtons;

    // UI elements
    private ProgressBar progressBar;
    private TextView tuneDraftsText;
    private TextView squadSizeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // region BOILERPLATE
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hot_100);

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

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "tune-database")
                .addCallback(callback)
                .build();

        sharedPrefs = this.getSharedPreferences(getResources().getString(R.string.preference_file_key), MODE_PRIVATE);
        // endregion

        draftButtons = new HashMap<>();

        // set element variables
        progressBar = findViewById(R.id.chartProgressBar);
        tuneDraftsText = findViewById(R.id.tuneDraftsText);
        squadSizeText = findViewById(R.id.squadText);

        updateHeaderText(); // update tune drafts text
        getHot100Chart(this::createChartList); // create hot 100 chart

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        sharedPrefs.edit().putInt(getResources().getString(R.string.tune_drafts_key), SessionData.getTuneDrafts()).apply(); // save tune drafts to shared preferences when activity is destroyed

    }

    private void getHot100Chart(ChartRetrievalListener listener) {

        Gson gson = new Gson();

        new Thread(() -> {

            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            try {

                StringBuilder result = new StringBuilder();
                URL url = new URL(getResources().getString(R.string.latest_chart_url));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // TODO: test if the below comment is true or if it was just connection issues
                connection.setConnectTimeout(100); // can be modified, if not set, connection never occurs on non emulator mobile devices
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

                    progressBar.setVisibility(View.GONE); // hide progress bar after chart is retrieved
                    listener.onChartRetrieved(chart);

                });
            } catch (MalformedURLException e) {
                Log.d("debug-error", "Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                Log.d("debug-error", "IO Exception: " + e.getMessage());
            }
        }).start();
    }

    private void createChartList(Hot100Chart chart) {

        LinearLayout chartList = findViewById(R.id.chartList);

        for (int i = 0; i < chart.getData().size(); i++) {

            // creation order: rank text -> draft button -> tune info layout -> tune element layout -> space
            // layout order: rank text -> tune info layout -> draft button
            // CONVENTION: create the most nested elements first | create the elements on the side first, then create the middle element and constrain it to the side elements

            Hot100Chart.Hot100ChartElement chartElement = chart.getData().get(i);
            Tune tune = new Tune(chartElement.getName(), chartElement.getArtist(), chartElement.getThisWeekRank());

            // region RANK TEXT
            TextView rankText = new TextView(this);
            rankText.setText(String.format("#%s", chartElement.getThisWeekRank()));
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
            draftButton.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement
            enableDraftButton(draftButton); // enable draft button by default (will be disabled later if necessary)

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

                if (SessionData.getTuneDrafts() > 0) { // make sure there are tune drafts remaining

                    appDatabase.addTune(tune); // add tune to database

                    SessionData.decrementTuneDrafts(); // decrement tune drafts
                    SessionData.incrementSquadSize(); // increment squad tunes

                    disableDraftButton(draftButton); // disable draft button after click (prevents double clicking as draft button updates are async and may not be immediate)
                    updateDraftButtonStates(); // update draft button states
                    updateHeaderText(); // update tune drafts text

                }
            });
            // endregion

            // region TUNE INFO LAYOUT (TUNE NAME & ARTIST NAME)
            LinearLayout tuneInfoLayout = new LinearLayout(this);
            tuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

            // region TUNE NAME TEXT
            TextView tuneNameText = new TextView(this);
            String tuneName = chartElement.getName();

            tuneNameText.setText(tuneName);
            tuneNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
            tuneNameText.setTextSize(24);
            tuneNameText.setEllipsize(TextUtils.TruncateAt.END);
            tuneNameText.setSingleLine();
            // endregion

            // region ARTIST NAME TEXT
            TextView artistNameText = new TextView(this);
            String artistName = chartElement.getArtist();

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

            chartList.addView(tuneElementLayout); // add tune element layout to tune list
            // endregion

            // region SPACE
            // only add space after element if it's not the last element in the chart
            if (i < chart.getData().size() - 1) {

                Space space = new Space(this);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64 // height of space in dp
                );
                space.setLayoutParams(spaceParams);

                chartList.addView(space); // add space to tune list

            }
            // endregion

            draftButtons.put(draftButton, tune); // add button & its tune to hashmap of draft buttons

        }

        updateDraftButtonStates(); // update draft button states at the very end

    }

    // TODO: add connection request failure error feedback
    // TODO: show none text if inventory is empty

    // region UI UTILITIES
    private void updateDraftButtonStates() {

        // disable all buttons if no tune drafts remain or squad is full
        if (SessionData.getTuneDrafts() == 0 || SessionData.getSquadSize() >= getResources().getInteger(R.integer.max_squad_size))
            draftButtons.forEach(
                    (key, value) -> disableDraftButton(key)
            );

        // disable button if tune already exists in database
        draftButtons.forEach((button, tune) -> appDatabase.containsTune(tune, tuneExists -> {
            if (tuneExists) disableDraftButton(button);
        }));

    }

    private void enableDraftButton(Button button) {

        button.setEnabled(true);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.draft_button_enabled_bg));
        button.setTextColor(getColor(R.color.draft_button_enabled_text));

    }

    private void disableDraftButton(Button button) {

        button.setEnabled(false);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.draft_button_disabled_bg));
        button.setTextColor(getColor(R.color.draft_button_disabled_text));

    }

    private void updateHeaderText() {

        tuneDraftsText.setText(String.format(getResources().getString(R.string.tune_drafts_text) + " %d", SessionData.getTuneDrafts()));
        squadSizeText.setText(String.format("%s %s/%s", getResources().getString(R.string.squad_text), SessionData.getSquadSize(), getResources().getInteger(R.integer.max_squad_size)));

    }
    // endregion

}