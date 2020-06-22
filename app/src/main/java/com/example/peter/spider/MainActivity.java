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

public class MainActivity extends Activity implements View.OnClickListener {

    private Button play, stats;
    private static final String TAG = "MainActivity";

    // These are the app life-cycles for different actions:

    // When initial gameView is created (after button pressed):
    // onResume(), GameView(context, difficulty),
    // gameView.surfaceCreated(), gameView.surfaceChanged()

    // When screen is rotated:
    // onConfigurationChanged(), gameView.updateOrientation(),
    // gameView.surfaceDestroyed(), gameView.surfaceCreated(),
    // gameView.surfaceChanged()

    // When back button is pressed:
    // onPause(), gameView.surfaceDestroyed(),

    // Return to app after back button pressed:
    // onCreate(), onResume()

    // When app leaves focus:
    // onPause(), onSaveInstanceState(), gameView.surfaceDestroyed()

    // When app returns to focus:
    // onResume(), gameView.surfaceCreated(), gameView.surfaceChanged()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.e(TAG, "onCreate()");
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        play = (Button) findViewById(R.id.button_play);
        stats = (Button) findViewById(R.id.button_stats);
        play.setOnClickListener(this);
        stats.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        /**
         * When a button is clicked on the menu screen, the corresponding activity is started.
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Make full screen
        if(view.getId() == play.getId()) {
            startActivity(new Intent(MainActivity.this, SelectDifficultyActivity.class));
        } else if(view.getId() == stats.getId()) {
            startActivity(new Intent(MainActivity.this, SelectStatActivity.class));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Avoid onCreate() call when screen is rotated
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        // Called when app is left. Save the state of the game.
        super.onPause();
        Log.e(TAG, "onPause()");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // NOTE: Not used, game is saved to file from onPause().
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // NOTE: Not used, saved game is retrieved when "Resume" is clicked.
        super.onRestoreInstanceState(savedInstanceState);
    }
}
