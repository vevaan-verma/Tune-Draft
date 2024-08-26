package com.cladcobra.tunedraft;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.cladcobra.tunedraft.database.DailyTune;
import com.cladcobra.tunedraft.listener.DailyTuneRetrievalListener;
import com.cladcobra.tunedraft.res.SessionData;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class DailyTuneActivity extends AppCompatActivity {

    // data storage
    private AppDatabase appDatabase;

    // UI elements
    private ProgressBar progressBar;
    private LinearLayout dailyTuneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // region BOILERPLATE
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_tune);

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
        // endregion

        // set element variables
        progressBar = findViewById(R.id.dailyTuneProgressBar);
        dailyTuneLayout = findViewById(R.id.dailyTuneLayout);

        appDatabase.getDailyTune((dailyTune) -> {

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // if the stored daily tune is null (hasn't been stored) or has a different date, store/refresh to a new daily tune and set the new current date
            if (dailyTune == null || !dailyTune.getDate().equals(currentDate)) {

                getRandomDailyTune((newDailyTune) -> {

                    appDatabase.setDailyTune(newDailyTune);
                    createDailyTuneElement(newDailyTune);

                });
            } else {

                createDailyTuneElement(dailyTune);
                progressBar.setVisibility(View.GONE); // hide progress bar if daily tune is already stored

            }
        });
    }

    private void getRandomDailyTune(DailyTuneRetrievalListener listener) {

        Gson gson = new Gson();

        // region VALID DATES RETRIEVAL
        new Thread(() -> {

            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            try {

                StringBuilder validDatesResult = new StringBuilder();
                URL validDatesUrl = new URL(getResources().getString(R.string.valid_dates_url));

                HttpURLConnection validDatesConnection = (HttpURLConnection) validDatesUrl.openConnection();
                validDatesConnection.setRequestMethod("GET");
                validDatesConnection.setConnectTimeout(100); // can be modified, if not set, connection never occurs on non emulator mobile devices
                validDatesConnection.connect();

                int validDatesResponseCode = validDatesConnection.getResponseCode();
                Log.d("debug-info", "Response Code: " + validDatesResponseCode);

                if (validDatesResponseCode == 200) {

                    Scanner scanner = new Scanner(validDatesUrl.openStream());

                    while (scanner.hasNext())
                        validDatesResult.append(scanner.nextLine());

                    scanner.close();

                } else {
                    Log.d("debug-error", "Failed to fetch data. Response Code: " + validDatesResponseCode);
                }

                String[] validDates = gson.fromJson(validDatesResult.toString(), String[].class);

                // region RANDOM DAILY TUNE RETRIEVAL
                String randomDate = validDates[(int) (Math.random() * validDates.length)]; // select a random date from the valid dates

                new Thread(() -> {

                    runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

                    try {

                        StringBuilder dailyTuneResult = new StringBuilder();
                        URL dailyTuneUrl = new URL(getResources().getString(R.string.date_chart_base_url) + randomDate + ".json");

                        HttpURLConnection dailyTuneConnection = (HttpURLConnection) dailyTuneUrl.openConnection();
                        dailyTuneConnection.setRequestMethod("GET");
                        dailyTuneConnection.setConnectTimeout(100); // can be modified, if not set, connection never occurs on non emulator mobile devices
                        dailyTuneConnection.connect();

                        int dailyTuneResponseCode = dailyTuneConnection.getResponseCode();
                        Log.d("debug-info", "Response Code: " + dailyTuneResponseCode);

                        if (dailyTuneResponseCode == 200) {

                            Scanner scanner = new Scanner(dailyTuneUrl.openStream());

                            while (scanner.hasNext())
                                dailyTuneResult.append(scanner.nextLine());

                            scanner.close();

                        } else {
                            Log.d("debug-error", "Failed to fetch data. Response Code: " + dailyTuneResponseCode);
                        }

                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        Hot100Chart chart = gson.fromJson(dailyTuneResult.toString(), Hot100Chart.class); // get chart from JSON
                        Hot100Chart.Hot100ChartElement chartElement = chart.getData().get((int) (Math.random() * chart.getData().size())); // select a random song from the chart
                        DailyTune dailyTune = new DailyTune(chartElement.getName(), chartElement.getRawArtist(), chartElement.getThisWeekRank(), currentDate); // create daily tune from chart element (IMPORTANT: make sure to use today's date, not the chart's date)

                        runOnUiThread(() -> {

                            progressBar.setVisibility(View.GONE);
                            listener.onDailyTuneRetrieved(dailyTune);

                        });
                    } catch (MalformedURLException e) {
                        Log.d("debug-error", "Malformed URL: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("debug-error", "IO Exception: " + e.getMessage());
                    }
                }).start();
                // endregion

            } catch (MalformedURLException e) {
                Log.d("debug-error", "Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                Log.d("debug-error", "IO Exception: " + e.getMessage());
            }
        }).start();
        // endregion

    }

    private void createDailyTuneElement(DailyTune dailyTune) {

        // creation order: rank text -> draft button -> daily tune info layout -> daily tune element layout -> space
        // layout order: rank text -> daily tune info layout -> draft button
        // CONVENTION: create the most nested elements first | create the elements on the side first, then create the middle element and constrain it to the side elements

        // region RANK TEXT
        TextView rankText = new TextView(this);
        rankText.setText(String.format("#%s", dailyTune.getRank()));
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

//                appDatabase.addTune(tune); // add tune to database
//
//                SessionData.decrementTuneDrafts(); // decrement tune drafts
//                SessionData.incrementSquadSize(); // increment squad tunes
//
//                updateDraftButtonStates(); // update draft button states
//                updateTuneDraftsText(); // update tune drafts text

            }
        });
        // endregion

        // region DAILY TUNE INFO LAYOUT (DAILY TUNE NAME & ARTIST NAME)
        LinearLayout dailyTuneInfoLayout = new LinearLayout(this);
        dailyTuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

        // TODO: make these comments consistent ("daily tune artist" or "artist name"?)
        // region DAILY TUNE NAME TEXT
        TextView dailyTuneNameText = new TextView(this);
        String dailyTuneName = dailyTune.getName();

        dailyTuneNameText.setText(dailyTuneName);
        dailyTuneNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
        dailyTuneNameText.setTextSize(24);
        dailyTuneNameText.setEllipsize(TextUtils.TruncateAt.END);
        dailyTuneNameText.setSingleLine();
        // endregion

        // region ARTIST NAME TEXT
        TextView artistNameText = new TextView(this);
        String artistName = dailyTune.getArtist();

        artistNameText.setText(artistName);
        artistNameText.setTypeface(ResourcesCompat.getFont(this, R.font.inconsolata));
        artistNameText.setTextSize(16);
        artistNameText.setEllipsize(TextUtils.TruncateAt.END);
        artistNameText.setSingleLine();
        // endregion

        // add tune and artist texts to layout
        dailyTuneInfoLayout.addView(dailyTuneNameText);
        dailyTuneInfoLayout.addView(artistNameText);

        // region DAILY TUNE INFO LAYOUT CONSTRAINTS
        // constrain daily tune info layout to: top, bottom of parent | end of hot 100 rank | start of draft button
        ConstraintLayout.LayoutParams dailyTuneInfoParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        dailyTuneInfoParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        dailyTuneInfoParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        dailyTuneInfoParams.startToEnd = rankText.getId();
        dailyTuneInfoParams.endToStart = draftButton.getId();
        dailyTuneInfoParams.leftMargin = 48;
        dailyTuneInfoParams.rightMargin = 48;
        dailyTuneInfoLayout.setLayoutParams(dailyTuneInfoParams);
        // endregion
        // endregion

        // region TUNE ELEMENT LAYOUT
        ConstraintLayout dailyTuneElementLayout = new ConstraintLayout(this);
        dailyTuneElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.chart_tune_element_bg));

        // add rank text, daily tune info layout, and draft button to tune element layout
        dailyTuneElementLayout.addView(rankText);
        dailyTuneElementLayout.addView(dailyTuneInfoLayout);
        dailyTuneElementLayout.addView(draftButton);

        dailyTuneLayout.addView(dailyTuneElementLayout); // add tune element layout to tune list
        // endregion

    }

    // region UI UTILITIES
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
    // endregion

}