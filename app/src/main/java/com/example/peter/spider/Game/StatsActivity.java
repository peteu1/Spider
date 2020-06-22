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
import com.example.peter.spider.SelectDifficultyActivity;
import com.example.peter.spider.SelectStatActivity;

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
    public static final String GAME_STATE_FILE_NAME = "game_state.txt";
    private String statsFileName;
    private File statsFile;
    private Button newGame, menu;
    private String currentTime, bestTime;
    private int difficulty, currentMoves, bestMoves, currentScore, bestScore;
    private int rankTime, rankMoves, rankScore;
    private boolean addToFile;  // whether or not to append these stats (don't add from SelectStatActivity)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        newGame = (Button) findViewById(R.id.new_game);
        menu = (Button) findViewById(R.id.menu);

        // Initialize access to Text Views in XML
        TextView difficultyText = (TextView) findViewById(R.id.tv_difficulty);
        TextView gamesPlayedText = (TextView) findViewById(R.id.tv_games_played);
        TextView currentTimeText = (TextView) findViewById(R.id.currentTime);
        TextView rankTimeText = (TextView) findViewById(R.id.rankTime);
        TextView bestTimeText = (TextView) findViewById(R.id.bestTime);
        TextView currentMovesText = (TextView) findViewById(R.id.currentMoves);
        TextView rankMovesText = (TextView) findViewById(R.id.rankMoves);
        TextView bestMovesText = (TextView) findViewById(R.id.bestMoves);
        TextView currentScoreText = (TextView) findViewById(R.id.currentScore);
        TextView rankScoreText = (TextView) findViewById(R.id.rankScore);
        TextView bestScoreText = (TextView) findViewById(R.id.bestScore);

        // Get current time and moves from Intent [from GameView.gameWon()]
        Intent i = getIntent();
        difficulty = i.getIntExtra("difficulty", 0);
        currentTime = i.getStringExtra("currentTime");
        currentMoves = i.getIntExtra("currentMoves", -1);
        currentScore = i.getIntExtra("currentScore", -1);
        addToFile = i.getBooleanExtra("addToFile", true);
        Log.e(TAG, "addToFile:" + addToFile);

        if (addToFile) {
            menu.setOnClickListener(this);
            newGame.setOnClickListener(this);
        } else {
            // Activity was called from SelectStatActivity
            menu.setVisibility(View.GONE);
            newGame.setVisibility(View.GONE);
        }

        // Add stats to file & get high scores, rank
        statsFileName = difficulty + STATS_HISTORY_FILE_NAME;
        File filePath = getExternalFilesDir(null);
        statsFile = new File(filePath, statsFileName);
        int gamesPlayed = addStats();

        // Update UI
        String strDifficulty = getDifficultyText(difficulty);
        difficultyText.setText(strDifficulty);
        gamesPlayedText.setText(String.valueOf(gamesPlayed));
        // Set current time, best time, and current time rank
        int gold = getResources().getColor(R.color.colorHighScore);
        if (currentTime.equals(bestTime)) {
            currentTimeText.setTextColor(gold);
            bestTimeText.setTextColor(gold);
            rankTimeText.setTextColor(gold);
        }
        currentTimeText.setText(currentTime);
        bestTimeText.setText(bestTime);
        rankTimeText.setText(String.valueOf(rankTime));

        // Set current moves, best moves, and current moves rank
        if (currentMoves == bestMoves) {
            currentMovesText.setTextColor(gold);
            bestMovesText.setTextColor(gold);
            rankMovesText.setTextColor(gold);
        }
        currentMovesText.setText(String.valueOf(currentMoves));
        bestMovesText.setText(String.valueOf(bestMoves));
        rankMovesText.setText(String.valueOf(rankMoves));

        // Set current score, best score, and current score rank
        if (currentScore == bestScore) {
            currentScoreText.setTextColor(gold);
            bestScoreText.setTextColor(gold);
            rankScoreText.setTextColor(gold);
        }
        currentScoreText.setText(String.valueOf(currentScore));
        bestScoreText.setText(String.valueOf(bestScore));
        rankScoreText.setText(String.valueOf(rankScore));

        // Ensure game state file was deleted
        // TODO: Better way to do this? File is re-created when mainActivity gets paused
        try {
            File file = new File(filePath, GAME_STATE_FILE_NAME);
            boolean deleted = file.delete();
            //Log.e(TAG, "Game state file deleted:" + deleted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == menu.getId()) {
            // Return to home screen
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if (view.getId() == newGame.getId()) {
            // Start new game with same difficulty
            Intent i = new Intent(this, SelectDifficultyActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("difficulty", difficulty);
            startActivity(i);
        }
    }

    private String getDifficultyText(int difficulty) {
        switch (difficulty) {
            case 1:
                return "Easy";
            case 2:
                return "Medium";
            case 3:
                return "Hard";
            case 4:
                return "Expert";
        }
        return "";
    }

    private int addStats() {
        // Adds current time/moves to stats history file and get current rank
        int currentSeconds = convertTime(currentTime);
        // Read stats file
        ArrayList<String> data = readStatsFile();
        // Get current rank & re-write output
        rankTime = 1; rankMoves = 1; rankScore = 1;
        int bestSeconds = currentSeconds;
        bestMoves = currentMoves;
        bestScore = currentScore;
        try {
            FileWriter writer = new FileWriter(statsFile);
            // Loop through lines of previous data
            for (String row : data) {
                String[] items = row.split(",");
                try {
                    int thisSeconds = Integer.parseInt(items[0]);
                    if (thisSeconds < currentSeconds) {
                        rankTime++;  // Increment rank
                        bestSeconds = Math.min(thisSeconds, bestSeconds);  // Update high score
                    }
                    int thisMoves = Integer.parseInt(items[1]);
                    if (thisMoves < currentMoves) {
                        rankMoves++;  // Increment rank
                        bestMoves = Math.min(thisMoves, bestMoves);  // Update high score
                    }
                    int thisScore = Integer.parseInt(items[2]);
                    if (thisScore < currentScore) {
                        rankScore++;
                        bestScore = Math.min(thisScore, bestScore);  // Update high score
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                writer.append(row + "\n");  // Add this line back to stats file
            }
            // Add new element to end of stats file (if game was just won)
            if (addToFile) {
                writer.append(currentSeconds + "," + currentMoves + "," + currentScore + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bestTime = convertTime(bestSeconds);
        return data.size() + (addToFile ? 1 : 0);
    }

    private ArrayList<String> readStatsFile() {
        // Read in current stats file
        int length = (int) statsFile.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(statsFile);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            //Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            //Log.e("login activity", "Can not read file: " + e.toString());
        }
        String rawData = new String(bytes);
        if (rawData.length() > 1) {
            String[] rows = rawData.split("\n");
            return new ArrayList<String>(Arrays.asList(rows));
        } else {  // no data
            return new ArrayList<String>();
        }
    }

    private int convertTime(String strTime) {
        String[] times = strTime.split(":");
        return (Integer.parseInt(times[0]) * 60) + Integer.parseInt(times[1]);
    }

    private String convertTime(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return minutes + ":" + ((seconds<10) ? "0" : "") + seconds;
    }
}
