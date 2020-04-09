package com.example.peter.spider;

import android.app.Activity;
import android.content.res.Configuration;
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
    private GameView gameView = null;

    // These are the app life-cycles for different actions:

    // When initial gameView is created (after button pressed):
    // onResume(), GameView(context, difficulty),
    // gameView.surfaceCreated(), gameView.surfaceChanged()

    // When screen is rotated:
    // onPause(), onSaveInstanceState(), gameView.surfaceDestroyed(),
    // onCreate(), GameView(context, savedInstance),
    // onRestoreInstanceState(), onResume(),
    // gameView.surfaceCreated(), gameView.surfaceChanged()

    // When back button is pressed:
    // onPause(), gameView.surfaceDestroyed(),

    // Return to app after back button pressed:
    // onCreate(), onResume()

    // When app leaves focus:
    // onPause(), onSaveInstanceState(), gameView.surfaceDestroyed()

    // When app returns to focus:
    // onResume(), gameView.surfaceCreated(), gameView.surfaceChanged()

    // TODO: retain data when app is killed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate()");
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        // TODO: Need this?
        //requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // TODO: Or if gameView was not in view when re-opening app/rotate screen
        if (savedInstanceState == null) {
            // TODO: Check record to see if there is an incomplete
            //  game, and add (unhide) resume if so.
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
            gameView = new GameView(this);
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
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged()");
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(TAG,"LANDSCAPE");
        } else {
            Log.e(TAG,"PORTRAIT");
        }
        super.onConfigurationChanged(newConfig);
        if (gameView != null) {
            Log.e(TAG, "gameView exists!");
            gameView.updateOrientation(this, newConfig.orientation);
            setContentView(gameView);
        } else {
            Log.e(TAG, "gameView DNE");
            // TODO: Set content view to menu? Or do nothing
        }
    }

    @Override
    protected void onPause() {
        // This is called when app is left and returned to, not onCreate()
        Log.e(TAG, "onPause()");
        // TODO: notify gameView when saveInstanceState() is called
        // TODO: Move this call to gameView.onSurfaceDestroyed()
        // Only call this when: (1) screen rotates, or (2) screen does
        //  not rotate and saveInstanceState never called [back pressed]
        // TODO: Store screen orientation in this class
        if (gameView != null) {
            gameView.killThread();
            gameView.storeGameState();
        }
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
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO: Remove
        Log.e(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
