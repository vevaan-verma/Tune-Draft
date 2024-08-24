package com.cladcobra.tunedraft;

import android.os.Bundle;
import android.text.TextUtils;
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
    private LinearLayout squadList;
    private TextView squadText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // region BOILERPLATE
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_squad);

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

        tuneDatabase = Room.databaseBuilder(getApplicationContext(), TuneDatabase.class, "tune-database")
                .addCallback(callback)
                .build();
        // endregion

        releaseButtonElements = new HashMap<>();

        // set element variables
        squadText = findViewById(R.id.squadText);
        updateSquadText();

        // set element backgrounds
        squadText.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_text_bg));

        createTuneList();

    }

    private void createTuneList() {

        squadList = findViewById(R.id.squadList);

        tuneDatabase.getAllTunes(tunes -> {

            for (Tune tune : tunes) {

                // creation order: tune info layout -> release button -> rank text -> squad element layout
                // layout order: tune info layout -> rank text -> release button
                // CONVENTION: create the most nested elements first | create the elements on the side first, then create the middle element and constrain it to the side elements

                // region RANK TEXT
                TextView rankText = new TextView(this);
                rankText.setText(String.format("#%s", tune.getRank()));
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

                // region RELEASE BUTTON
                Button releaseButton = new Button(this);
                releaseButton.setText(R.string.release_tune_text);
                releaseButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.release_button_bg));
                releaseButton.setId(View.generateViewId()); // IMPORTANT: set id for constraint layout placement

                // constrain release button to: top, bottom, end of parent
                ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                buttonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                buttonParams.rightMargin = 48;
                releaseButton.setLayoutParams(buttonParams);

                releaseButton.setOnClickListener(v -> {

                    tuneDatabase.removeTune(tune); // remove tune from database and update squad text when tune is removed
                    SessionData.decrementSquadSize(); // decrement squad size

                    SquadElementLayout elementLayout = releaseButtonElements.get(releaseButton);

                    if (elementLayout != null) {

                        // remove squad element layout and space
                        squadList.removeView(elementLayout.getLayout());
                        squadList.removeView(elementLayout.getSpace());

                        updateSquadText();

                    }
                });
                // endregion

                // region TUNE INFO LAYOUT (TUNE NAME & ARTIST NAME)
                LinearLayout tuneInfoLayout = new LinearLayout(this);
                tuneInfoLayout.setOrientation(LinearLayout.VERTICAL);

                // region TUNE NAME TEXT
                TextView tuneNameText = new TextView(this);
                String tuneName = tune.getName();

                tuneNameText.setText(String.format("%s", tuneName));
                tuneNameText.setTextSize(24);
                tuneNameText.setEllipsize(TextUtils.TruncateAt.END);
                tuneNameText.setSingleLine();
                // endregion

                // region ARTIST NAME TEXT
                TextView artistNameText = new TextView(this);
                String artistName = tune.getArtist();

                artistNameText.setText(String.format("%s", artistName));
                artistNameText.setTextSize(16);
                artistNameText.setEllipsize(TextUtils.TruncateAt.END);
                artistNameText.setSingleLine();
                // endregion

                // add tune and artist texts to layout
                tuneInfoLayout.addView(tuneNameText);
                tuneInfoLayout.addView(artistNameText);

                // region TUNE INFO LAYOUT CONSTRAINTS
                // constrain tune info layout to: top, bottom of parent | end of hot 100 rank | start of release button
                ConstraintLayout.LayoutParams tuneInfoParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                tuneInfoParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                tuneInfoParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                tuneInfoParams.startToEnd = rankText.getId();
                tuneInfoParams.endToStart = releaseButton.getId();
                tuneInfoParams.leftMargin = 48;
                tuneInfoParams.rightMargin = 48;
                tuneInfoLayout.setLayoutParams(tuneInfoParams);
                // endregion
                // endregion

                // region SQUAD ELEMENT LAYOUT
                ConstraintLayout squadElementLayout = new ConstraintLayout(this);
                squadElementLayout.setBackground(AppCompatResources.getDrawable(this, R.drawable.squad_tune_element_bg));

                // add tune info, rank text, and release button to layout
                squadElementLayout.addView(tuneInfoLayout);
                squadElementLayout.addView(rankText);
                squadElementLayout.addView(releaseButton);

                squadList.addView(squadElementLayout); // add squad element layout to squad layout
                // endregion

                // region SPACE
                Space space = new Space(this);
                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        64 // height of space in dp
                );
                space.setLayoutParams(spaceParams);

                squadList.addView(space); // add space to squad list
                // endregion

                releaseButtonElements.put(releaseButton, new SquadElementLayout(squadElementLayout, space)); // store layout and space for removal

            }
        });
    }

    // region UI UTILITIES
    private void updateSquadText() {

        squadText.setText(String.format("%s (%s/%s)", getResources().getString(R.string.squad_text), SessionData.getSquadSize(), getResources().getInteger(R.integer.max_squad_size)));

    }
    // endregion

}