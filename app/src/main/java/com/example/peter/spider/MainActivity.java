package com.example.peter.spider;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.peter.spider.Game.GameView;

public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = "MainActivity";
    private Button easy, medium, hard, expert;
    private GameView gameView;

    // When screen is rotated, lifecycle is like this:
    // onPause(), onSaveInstanceState(), onCreate(),
    // onRestoreInstanceState(), onResume()

    // TODO: retain data when app is killed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate()");
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState == null) {
            Log.e(TAG, "Loading menu...");
            setContentView(R.layout.activity_main);

            easy = (Button) findViewById(R.id.easy);
            medium = (Button) findViewById(R.id.medium);
            hard = (Button) findViewById(R.id.hard);
            expert = (Button) findViewById(R.id.expert);

            easy.setOnClickListener(this);
            medium.setOnClickListener(this);
            hard.setOnClickListener(this);
            expert.setOnClickListener(this);
        } else {
            // Restore game from savedInstanceState
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Make full screen
            gameView = new GameView(this, savedInstanceState);
            setContentView(gameView);
        }
    }

    @Override
    public void onClick(View view) {
        /**
         * When a button is clicked on the menu screen, the game is launched
         *  with the corresponding difficulty
         */
        int difficulty = -1;
        if(view.getId() == easy.getId()) {
            difficulty = 1;
        } else if(view.getId() == medium.getId()) {
            difficulty = 2;
        } else if(view.getId() == hard.getId()) {
            difficulty = 3;
        } else if(view.getId() == expert.getId()) {
            difficulty = 4;
        }
        if (difficulty > 0) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Make full screen
            gameView = new GameView(this, difficulty);
            setContentView(gameView);
        }
    }

    @Override
    protected void onPause() {
        // TODO: Remove
        Log.e(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO: Remove
        Log.e(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Log.e(TAG, "onSaveInstanceState()");
        savedInstanceState = gameView.saveInstance(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO: Remove
        Log.e(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
