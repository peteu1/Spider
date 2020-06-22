package com.example.peter.spider;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.peter.spider.Game.GameView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SelectDifficultyActivity extends Activity implements View.OnClickListener {
    /**
     * This is the screen where you can select the difficulty or resume existing game.
     * Called when "Play" is clicked from Main Activity.
     */

    private static final String TAG = "SelectDifActivity";
    private Button easy, medium, hard, expert, resume;
    private GameView gameView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_game);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Make full screen

        // Check if new game was clicked from stat activity and get difficulty
        Intent i = getIntent();
        int difficulty = i.getIntExtra("difficulty", -1);
        if (difficulty > 0) {
            Log.e(TAG, "New game started with difficulty: " + difficulty);
            gameView = new GameView(this, difficulty);
            setContentView(gameView);
        } else {
            initialize();
        }
    }

    void initialize() {
        easy = (Button) findViewById(R.id.easy);
        medium = (Button) findViewById(R.id.medium);
        hard = (Button) findViewById(R.id.hard);
        expert = (Button) findViewById(R.id.expert);
        resume = (Button) findViewById(R.id.resume);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);
        expert.setOnClickListener(this);
        resume.setOnClickListener(this);

        // Check if there is saved game data
        boolean activeGame = checkForActiveGame();
        //Log.e(TAG, "Active game:" + activeGame);
        resume.setVisibility(activeGame ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onClick(View view) {
        /**
         * When a button is clicked on the menu screen, the game is launched
         *  with the corresponding difficulty (or resume current game)
         */
        // Create game depending on button pressed
        if(view.getId() == easy.getId()) {
            gameView = new GameView(this, 1);
        } else if(view.getId() == medium.getId()) {
            gameView = new GameView(this, 2);
        } else if(view.getId() == hard.getId()) {
            gameView = new GameView(this, 3);
        } else if(view.getId() == expert.getId()) {
            gameView = new GameView(this, 4);
        } else if (view.getId() == resume.getId()) {
            // Resume: restore from saved game state
            gameView = new GameView(this);
        }
        // Launch game view
        setContentView(gameView);
    }


    private boolean checkForActiveGame() {
        /**
         * Looks at the saved game to see if there is an incomplete
         * game that can be resumed.
         */
        File filePath = getExternalFilesDir(null);
        File file = new File(filePath, GameView.GAME_STATE_FILE_NAME);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } catch (Exception e1) {
                return false;
            } finally {
                in.close();
            }
        } catch (IOException e) {  // FileNotFoundException e
            return false;
        }
        return true;  // bytes.length > 0;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Avoid onCreate() call when screen is rotated
        super.onConfigurationChanged(newConfig);
        if (gameView != null) {
            gameView.updateOrientation(this);
            setContentView(gameView);
        }
    }

    @Override
    protected void onPause() {
        // Called when app is left. Save the state of the game.
        Log.e(TAG, "onPause()");
        if (gameView != null) {
            gameView.killThread();
            gameView.storeGameState();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // NOTE: Not used, game is saved to file from onPause().
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // NOTE: Not used, saved game is retrieved when "Resume" is clicked.
        //Log.e(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
