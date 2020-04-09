package com.example.peter.spider.Game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.peter.spider.MainActivity;
import com.example.peter.spider.R;

import java.io.File;
import java.io.FileWriter;

public class StatsActivity extends Activity implements View.OnClickListener {

    private final String TAG = "StatsActivity";
    public static final String STATS_HISTORY_FILE_NAME = "stats_history.txt";
    private Button menu;
    private TextView currentTimeText;
    private TextView currentMovesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        currentTimeText = (TextView) findViewById(R.id.currentTime);
        // TODO: Add other text views: rankTime, bestTime
        currentMovesText = (TextView) findViewById(R.id.currentMoves);
        // rankMoves, bestMoves

        menu = (Button) findViewById(R.id.menu);
        menu.setOnClickListener(this);

        Intent i = getIntent();
        String completedTime = i.getStringExtra("completedTime");
        int totalMoves = i.getIntExtra("totalMoves", -1);
        Log.e(TAG, "completedTime:" + completedTime);
        addStats(completedTime, totalMoves);
        currentTimeText.setText(completedTime);
        currentMovesText.setText(totalMoves);
    }

    private void addStats(String completedTime, int totalMoves) {
        // Add stats to database/file
        File filePath = getExternalFilesDir(null);
        try {
            File statsFile = new File(filePath, STATS_HISTORY_FILE_NAME);
            FileWriter writer = new FileWriter(statsFile);
            String newLine = completedTime + "," + totalMoves;
            writer.append(newLine + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == menu.getId()) {
            // Return to home screen
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
}
