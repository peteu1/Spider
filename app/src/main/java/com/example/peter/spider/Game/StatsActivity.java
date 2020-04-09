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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class StatsActivity extends Activity implements View.OnClickListener {

    private final String TAG = "StatsActivity";
    // NOTE: Actual files will be prepended with difficulty integer
    public static final String STATS_HISTORY_FILE_NAME = "_stats_history.txt";
    public static final String HIGH_SCORE_FILE_NAME = "_high_score.txt";
    private int difficulty;
    private Button menu;
    private String currentTime, bestTime;
    private int currentMoves, bestMoves;
    private int rankTime, rankMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        menu = (Button) findViewById(R.id.menu);
        menu.setOnClickListener(this);

        TextView currentTimeText = (TextView) findViewById(R.id.currentTime);
        TextView bestTimeText = (TextView) findViewById(R.id.bestTime);
        TextView rankTimeText = (TextView) findViewById(R.id.rankTime);
        TextView currentMovesText = (TextView) findViewById(R.id.currentMoves);
        TextView bestMovesText = (TextView) findViewById(R.id.bestMoves);
        TextView rankMovesText = (TextView) findViewById(R.id.rankMoves);

        // Get current time and moves from Intent [from GameView.gameWon()]
        Intent i = getIntent();
        difficulty = i.getIntExtra("difficulty", 0);
        currentTime = i.getStringExtra("currentTime");
        currentMoves = i.getIntExtra("currentMoves", -1);
        Log.e(TAG, "currentTime:" + currentTime);

        // Add stats to file & get/update high scores
        addStats();
        getHighScores();
        // Update scores in UI
        if (currentTime.equals(bestTime)) {
            // TODO: Use gold font color
            currentTimeText.setText(currentTime);
            bestTimeText.setText(bestTime);
        }
        if (currentMoves == bestMoves) {
            // TODO: Use gold font color
            currentMovesText.setText(String.valueOf(currentMoves));
            bestMovesText.setText(String.valueOf(bestMoves));
        }
        rankTimeText.setText(String.valueOf(rankTime));
        rankMovesText.setText(String.valueOf(rankMoves));
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

    private void addStats() {
        /**
         * Adds current time/moves to stats history file
         */
        File filePath = getExternalFilesDir(null);
        String statsFileName = String.valueOf(difficulty) + STATS_HISTORY_FILE_NAME;
        // Add stats to file
        File statsFile = new File(filePath, statsFileName);
        // TODO: First need to read everything, then re-write with new row
        try {
            FileWriter writer = new FileWriter(statsFile);
            String newLine = currentTime + "," + currentMoves;
            writer.append(newLine + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Read through stats file to get rank
        int length = (int) statsFileName.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(statsFile);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        String rawData = new String(bytes);
        String[] rows = rawData.split("\n");
        ArrayList<String> data = new ArrayList<String>(Arrays.asList(rows));
        Log.e(TAG, "rawData:" + rawData);
        rankTime = 1;
        rankMoves = 1;
        int currentSeconds = convertTime(currentTime);
        for (String row : data) {
            String[] items = row.split(",");
            try {
                if (convertTime(items[0]) < currentSeconds) {
                    rankTime++;
                }
                if (Integer.parseInt(items[1]) < currentMoves) {
                    rankMoves++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // TODO: Don't need high score file? Set font gold if rank = 1?
    }

    private void getHighScores() {
        /**
         * Gets best time/moves from high score file
         * Updates high score file if new high score
         */
        File filePath = getExternalFilesDir(null);
        String highScoreFileName = String.valueOf(difficulty) + HIGH_SCORE_FILE_NAME;
        File highScoreFile = new File(filePath, highScoreFileName);
        int length = (int) highScoreFile.length();
        Log.e(TAG, "highScoreFile length:" + length);
        if (length > 0) {
            // Get current high scores
            byte[] bytes = new byte[length];
            try {
                FileInputStream in = new FileInputStream(highScoreFile);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
            String rawData = new String(bytes);
            String[] items = rawData.split(",");
            bestTime = items[0];
            bestMoves = Integer.parseInt(items[1]);
            // Update high scores if broken
            if (convertTime(currentTime) < convertTime(bestTime)) {
                bestTime = currentTime;
            }
            if (currentMoves < bestMoves) {
                bestMoves = currentMoves;
            }
        } else {
            // First time playing this difficulty -> new record
            bestTime = currentTime;
            bestMoves = currentMoves;
        }
        // Update high score file if any new high score
        if ((currentTime.equals(bestTime)) || (currentMoves == bestMoves)) {
            try {
                highScoreFile.delete();
                FileWriter highScorewriter = new FileWriter(highScoreFile);
                String newHighScore = bestTime + "," + bestMoves;
                highScorewriter.append(newHighScore);
                highScorewriter.flush();
                highScorewriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int convertTime(String strTime) {
        String[] times = strTime.split(":");
        return (Integer.parseInt(times[0]) * 60) + Integer.parseInt(times[1]);
    }
}
