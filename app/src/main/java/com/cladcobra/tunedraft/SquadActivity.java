package com.cladcobra.tunedraft;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import com.cladcobra.tunedraft.database.Tune;
import com.cladcobra.tunedraft.database.TuneDatabase;
import com.cladcobra.tunedraft.res.SessionData;
import com.cladcobra.tunedraft.util.SquadElementLayout;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SquadActivity extends AppCompatActivity {

    // data storage
    private TuneDatabase tuneDatabase;
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
        tuneDatabase = Room.databaseBuilder(getApplicationContext(), TuneDatabase.class, "tune-database")
                .addCallback(callback)
                .build();

        releaseButtonElements = new HashMap<>();

        // set element variables
        squadText = findViewById(R.id.squadText);
        squadText.setText(String.format("%s (%s/%s)", getResources().getString(R.string.squad_text), SessionData.getSquadSize(), getResources().getInteger(R.integer.max_squad_size)));

        // set element backgrounds
        squadText.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_text_bg));

        createTuneList();

    }

    private void createTuneList() {

        squadLayout = findViewById(R.id.squadLayout);

        tuneDatabase.getAllTunes(tunes -> {

            for (Tune tune : tunes) {

                // space between each tune info element
                Space space = new Space(this);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64 // height of space in dp
                );
                space.setLayoutParams(spaceParams);

                ConstraintLayout squadElementLayout = new ConstraintLayout(this);
                squadElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_tune_element_bg));

                LinearLayout tuneInfoLayout = new LinearLayout(this);
                tuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

                TextView tuneNameText = new TextView(this);
                String tuneName = tune.getName();

                // clamp tune name to max length
                int maxTuneChars = getResources().getInteger(R.integer.max_tune_chars);

                if (tuneName.length() > maxTuneChars)
                    tuneName = tuneName.substring(0, maxTuneChars) + "...";

                tuneNameText.setText(String.format("%s", tuneName));
                tuneNameText.setPadding(48, 0, 0, 0);
                tuneNameText.setTextSize(24);

                TextView artistNameText = new TextView(this);
                String artistName = tune.getArtist();

                // clamp artist name to max length
                int maxArtistChars = getResources().getInteger(R.integer.max_artist_chars);

                if (artistName.length() > maxArtistChars)
                    artistName = artistName.substring(0, maxArtistChars) + "...";

                artistNameText.setText(String.format("%s", artistName));
                artistNameText.setPadding(48, 0, 0, 0);
                artistNameText.setTextSize(16);

                // hot 100 rank text
                TextView hot100Rank = new TextView(this);
                hot100Rank.setText(String.format("#%s", tune.getRank()));
                hot100Rank.setTypeface(ResourcesCompat.getFont(this, R.font.rem));
                hot100Rank.setTextColor(getResources().getColor(R.color.hot_100_rank_color, null));
                hot100Rank.setTextSize(30);

                // release button
                Button releaseButton = createReleaseButton(tune);
                releaseButtonElements.put(releaseButton, new SquadElementLayout(squadElementLayout, space)); // store layout and space for removal

                // constrain tune info layout to the left of hot 100 rank
                ConstraintLayout.LayoutParams rankParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                rankParams.endToStart = releaseButton.getId();
                rankParams.rightMargin = 48;
                hot100Rank.setLayoutParams(rankParams);

                tuneInfoLayout.addView(tuneNameText);
                tuneInfoLayout.addView(artistNameText);

                squadElementLayout.addView(tuneInfoLayout);
                squadElementLayout.addView(hot100Rank);
                squadElementLayout.addView(releaseButton);

                squadLayout.addView(squadElementLayout);

                squadLayout.addView(space);

            }
        });
    }

    private Button createReleaseButton(Tune tune) {

        // TODO: find more efficient way to store drafts remaining?

        Button button = new Button(this);
        button.setText(R.string.release_tune_text);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.release_button_bg));
        button.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement

        // constrain draft button to the right/end
        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        buttonParams.rightMargin = 48;
        button.setLayoutParams(buttonParams);

        button.setOnClickListener(v -> {

            tuneDatabase.removeTune(tune); // remove tune from database

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