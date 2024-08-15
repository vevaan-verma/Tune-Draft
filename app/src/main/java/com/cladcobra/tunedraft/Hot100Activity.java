package com.cladcobra.tunedraft;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cladcobra.tunedraft.chart.Hot100Chart;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Hot100Activity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;
    private ArrayList<Button> draftButtons;
    private TextView tunesRemainingText;
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

        draftButtons = new ArrayList<>();
        sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        // set element variables
        tunesRemainingText = findViewById(R.id.tunesRemainingText);
        progressBar = findViewById(R.id.progressBar);

        updateTunesRemaining(); // updates tunes remaining text
        getHot100(); // sets up hot 100 chart data

    }

    private void getHot100() {

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

        LinearLayout songListLayout = findViewById(R.id.songLayout);
        int rank = 1;

        for (Hot100Chart.Hot100ChartData chartData : chart.getData()) {

            LinearLayout innerLayout = getInnerLayout(chartData); // inner linear vertical layout
            LinearLayout outerLayout = getOuterLayout(innerLayout, rank); // outer linear horizontal layout

            songListLayout.addView(outerLayout); // add outer layout to song list layout

            // add space between each song element
            Space space = new Space(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    64 // height of space in dp
            );
            space.setLayoutParams(params);
            songListLayout.addView(space);

            rank++; // increment rank

        }
    }

    /* RANK & SONG INFO CONTAINER */
    private LinearLayout getOuterLayout(LinearLayout innerLayout, int rank) {

        LinearLayout outerLayout = new LinearLayout(this);
        outerLayout.setOrientation(LinearLayout.HORIZONTAL);
        outerLayout.setBackgroundColor(Color.argb(255, 0, 0, 0));
        outerLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.song_bg));
        outerLayout.setGravity(Gravity.CENTER_VERTICAL);

        // hot 100 rank text
        TextView hot100Rank = new TextView(this);
        hot100Rank.setText(String.format("%s", rank));
        hot100Rank.setPadding(48, 0, 0, 0);
        hot100Rank.setTextSize(30);

        Button button = getDraftButton(); // button to draft tune
        draftButtons.add(button); // add button to list of draft buttons

        outerLayout.addView(hot100Rank);
        outerLayout.addView(innerLayout); // add inner layout to outer layout
        outerLayout.addView(button);

        return outerLayout;

    }

    private Button getDraftButton() {

        // TODO: find more efficient way to store tunes remaining?

        Button button = new Button(this);
        button.setText(R.string.draft_tune_text);

        AtomicInteger tunesRemaining = new AtomicInteger(sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0));

        // disable all buttons if no tunes drafts remain
        if (tunesRemaining.get() == 0)
            for (Button draftButton : draftButtons)
                draftButton.setEnabled(false);

        button.setOnClickListener(v -> {

            tunesRemaining.set(sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0));

            if (tunesRemaining.get() > 0) {

                // disable all buttons if no tunes drafts remain
                if (tunesRemaining.get() == 1) // this would mean there are no tunes remaining now
                    for (Button draftButton : draftButtons)
                        draftButton.setEnabled(false);

                sharedPrefs.edit().putInt(getString(R.string.drafts_remaining_key), tunesRemaining.get() - 1).apply();
                updateTunesRemaining();

            }
        });

        return button;

    }

    /* SONG INFO */
    private LinearLayout getInnerLayout(Hot100Chart.Hot100ChartData chartData) {

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        // song name text
        TextView songName = new TextView(this);
        songName.setText(chartData.getSongName());
        songName.setPadding(48, 0, 0, 0);
        songName.setTextSize(20);

        // artist name text
        TextView artistName = new TextView(this);
        artistName.setText(chartData.getArtist());
        artistName.setPadding(48, 0, 0, 0);

        // add song and artist to layout
        innerLayout.addView(songName);
        innerLayout.addView(artistName);
        return innerLayout;

    }

    private void updateTunesRemaining() {

        int tunesRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);
        tunesRemainingText.setText(String.format(getString(R.string.tunes_remaining) + " %d", tunesRemaining));

    }

    private void saveData() {

//        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
//
//        // gets the data repository in write mode
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        // create a new map of values, where column names are the keys
//        ContentValues values = new ContentValues();
//        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);
//        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
//
//        // insert the new row, returning the primary key value of the new row
//        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

    }
}