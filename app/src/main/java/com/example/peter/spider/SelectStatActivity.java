package com.example.peter.spider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.peter.spider.Game.StatsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SelectStatActivity extends Activity implements View.OnClickListener {
    /**
     * This is the screen where you can select the difficulty of stats to view
     * Called when "Stats" is clicked from Main Activity.
     */

    private static final String TAG = "SelectStatActivity";
    private Button easy, medium, hard, expert, last;
    public static final String PREV_GAME_FILE_NAME = "prev_game.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_stat);

        easy = (Button) findViewById(R.id.button_stat_easy);
        medium = (Button) findViewById(R.id.button_stat_medium);
        hard = (Button) findViewById(R.id.button_stat_hard);
        expert = (Button) findViewById(R.id.button_stat_expert);
        last = (Button) findViewById(R.id.button_stat_last_game);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);
        expert.setOnClickListener(this);
        last.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //if (view.getId() == easy.getId()) {
        // TODO: need new activity to show stats given a difficulty with back button
        if (view.getId() == last.getId()) {
            Intent i = getLastStats();
            if (i != null) {
                startActivity(i);
            } else {
                Toast.makeText(this, "No data found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Intent getLastStats() {
        // Reads PREV_GAME_FILE_NAME and parses information necessary for StatsActivity
        File filePath = getExternalFilesDir(null);
        File file = new File(filePath, PREV_GAME_FILE_NAME);
        byte[] bytes = new byte[(int) file.length()];
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
        } catch (IOException e) {  // FileNotFoundException e
            return null;
        }
        String rawData = new String(bytes);
        String[] items = rawData.split("\n");
        // Extract data needed for stats activity into an intent
        Intent i = new Intent(SelectStatActivity.this, StatsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("difficulty", Integer.parseInt(items[0].split(",")[1]));
        int millis = Integer.parseInt(items[2].split(",")[1]);
        Log.e(TAG, "millis:" + millis);
        String currentTime = convertMillis(millis);
        Log.e(TAG, "currentTime:" + currentTime);
        i.putExtra("currentTime", currentTime);
        i.putExtra("currentMoves", Integer.parseInt(items[3].split(",")[1]));
        i.putExtra("currentScore", Integer.parseInt(items[4].split(",")[1]));
        i.putExtra("addToFile", false);
        return i;
    }

    private String convertMillis(int totalMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMillis) % 60;
        String strSeconds = ((seconds<10) ? "0" : "") + seconds;
        return minutes + ":" + strSeconds;
    }

}
