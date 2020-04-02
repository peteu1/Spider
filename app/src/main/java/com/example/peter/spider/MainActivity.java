package com.example.peter.spider;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button easy, medium, hard, expert;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Make full screen
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(new GameView(this));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Launches the Menu screen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        easy = (Button) findViewById(R.id.easy);
        medium = (Button) findViewById(R.id.medium);
        hard = (Button) findViewById(R.id.hard);
        expert = (Button) findViewById(R.id.expert);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);
        expert.setOnClickListener(this);
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
            setContentView(new GameView(this, difficulty));
        }
    }

    // TODO: retain data on Pause() and when app killed
}
