package com.cladcobra.tunedraft;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        Button addDraftsButton = findViewById(R.id.addDraftsButton);
        addDraftsButton.setOnClickListener(v -> AddTuneDraft());

        Button chartButton = findViewById(R.id.billboardHot100);
        chartButton.setOnClickListener(v -> LoadChart());

    }

    private void AddTuneDraft() {

        int tunesRemaining = sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0);
        sharedPrefs.edit().putInt(getString(R.string.drafts_remaining_key), tunesRemaining + 1).apply();
        Log.d("debug-info", sharedPrefs.getInt(getString(R.string.drafts_remaining_key), 0) + "");

    }

    private void LoadChart() {

        Intent intent = new Intent(this, Hot100Activity.class);
        startActivity(intent);

    }
}