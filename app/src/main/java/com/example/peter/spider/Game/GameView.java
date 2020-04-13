package com.example.peter.spider.Game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.peter.spider.MainActivity;
import com.example.peter.spider.MainThread;
import com.example.peter.spider.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    public static final String GAME_STATE_FILE_NAME = "game_state.txt";
    private MainThread thread;
    private Context context;
    DisplayMetrics displayMetrics;
    public Master master;
    private Paint textPaint;
    private boolean cardsInMotion;
    private int screenWidth, screenHeight;

    public GameView(Context context, int difficulty) {
        // Constructor for new game
        super(context);
        Log.e(TAG, "New constructor called.");
        HashMap<Integer, Drawable> mStore = initialize(context);
        // Initialize Master
        master = new Master(screenWidth, screenHeight, difficulty, mStore);
    }

    public GameView(Context context) {
        // Constructor to restore game where it left off.
        super(context);
        Log.e(TAG, "Restore constructor called.");
        HashMap<Integer, Drawable> mStore = initialize(context);
        // Re-create master
        ArrayList<String> savedData = readSavedData();
        master = new Master(screenWidth, screenHeight, mStore, savedData);
    }

    private void updateDisplayMetrics() {
        displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    private HashMap<Integer, Drawable> initialize(Context context) {
        // Helper method used by both constructors
        this.context = context;
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
        // Get display metrics (screen width & height)
        updateDisplayMetrics();
        initPaints();
        // Initialize Image HashMap
        HashMap<Integer, Drawable> mStore = new HashMap<Integer, Drawable>();
        Drawable cardBack = getResources().getDrawable(R.drawable.card_back, null);
        mStore.put(R.id.card_back, cardBack);
        Drawable spades = getResources().getDrawable(R.drawable.suit_spades, null);
        mStore.put(R.id.suit_spades, spades);
        Drawable hearts = getResources().getDrawable(R.drawable.suit_hearts, null);
        // TODO: Don't load un-needed suits (based on difficulty)
        mStore.put(R.id.suit_hearts, hearts);
        Drawable diamonds = getResources().getDrawable(R.drawable.suit_diamonds, null);
        mStore.put(R.id.suit_diamonds, diamonds);
        Drawable clubs = getResources().getDrawable(R.drawable.suit_clubs, null);
        mStore.put(R.id.suit_clubs, clubs);
        return mStore;
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(Color.rgb(30,30,30));
        textPaint.setTextSize(30);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated()");
        cardsInMotion = false;
        if (thread == null) {
            Log.e(TAG, "Re-creating thread");
            getHolder().addCallback(this);
            thread = new MainThread(getHolder(), this);
        } else {
            Log.e(TAG, "Thread already exists!!");
        }
        if (!thread.getRunning()) {
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        Log.e(TAG, "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed()");
    }

    @Override
    public void draw(Canvas canvas) {
        // This is called continuously right after update() is called
        super.draw(canvas);
        if (canvas != null) {
            // Fill background
            canvas.drawColor(getResources().getColor(R.color.colorBackground));
            // Draw move count
            String moves = String.valueOf(master.historyTracker.getNumMoves());
            canvas.drawText("Moves: " + moves, 15, textPaint.getTextSize()+5, textPaint);
            // Draw menu "button" (TODO: Properly center text)
            canvas.drawText("Menu", (int) (screenWidth/2) - 25, textPaint.getTextSize()+5, textPaint);
            // TODO: Use undo icon instead
            canvas.drawText("UNDO", 15, screenHeight-40, textPaint);
            // Draw time - bottom center
            String time = master.historyTracker.getTimeElapsed();
            canvas.drawText(time, (int) (screenWidth/2) - 25, screenHeight-40, textPaint);
            // Tell master to draw the stacks
            master.draw(canvas);
        }
    }

    public void killThread() {
        if (thread != null) {
            thread.setRunning(false);
            thread = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Can't do anything while card stack is being animated
        if (master.locked) {
            return true;
        }
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Initial touch of screen
                Log.e("action", "Initial Touch");
                // Check if a legal touch was initiated
                cardsInMotion = master.legalTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                // Touch is dragging
                if (cardsInMotion) {
                    master.moveStack(x, y);
                }
                // Do nothing when initial touch not valid (no cards in motion)
                break;
            case MotionEvent.ACTION_UP:
                // touch was released
                Log.e("action", "Touch released, x:" + String.valueOf(x) + ", y:" + String.valueOf(y));
                if (cardsInMotion) {
                    // Lock into place if legal move, else go back to initial location
                    boolean gameOver = master.endStackMotion(x, y);
                    cardsInMotion = false;
                    if (gameOver) {
                        gameWon();
                    }
                } else {
                    // TODO: Better approach than hard-coding some of these?
                    int centerX = master.screenWidth / 2;
                    // Check if undo "button" was touched
                    if ((x < 120) && (y > (master.screenHeight-100))) {
                        Log.e(TAG, "Undo clicked!");
                        if (!master.undo()) {
                            Toast.makeText(context, "Nothing to undo...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Check if menu "button" was touched
                    else if ((y < 50) && (x>(centerX-50)) && (x<(centerX+50))) {
                        // Return to home screen
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }
                    // TODO: Remove, testing game over
                    else if ((y > (master.screenHeight-100)) && (x>120)) {
                        gameWon();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e("action", "Cancelled");
                if (cardsInMotion) {
                    master.endStackMotion(-9999, -9999);
                }
                cardsInMotion = false;
                break;
            default:
                break;
        }
        return true; //super.onTouchEvent(event) || handled;
    }

    public void updateOrientation(Context context) {
        // Called when the screen is rotated
        this.context = context;
        updateDisplayMetrics();
        master.updateOrientation(screenWidth, screenHeight);
    }

    public void update() {
        // This is called continuously right before draw() is called
    }

    private void gameWon() {
        // Called when the game is won
        Log.e(TAG, "You win!");
        Toast.makeText(context, "You Win!", Toast.LENGTH_SHORT).show();
        // Delete saved data in GAME_STATE_FILE_NAME
        File filePath = context.getExternalFilesDir(null);
        try {
            File file = new File(filePath, GAME_STATE_FILE_NAME);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Get stats, add to intent & launch game won screen
        String currentTime = master.historyTracker.getTimeElapsed();
        int currentMoves = master.historyTracker.getNumMoves();
        // TODO: Get score
        Intent i = new Intent(context, StatsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("difficulty", master.difficulty);
        i.putExtra("currentTime", currentTime);
        i.putExtra("currentMoves", currentMoves);
        context.startActivity(i);
    }

    public void storeGameState() {
        // Called from onPause(), stores game state data to file.
        Log.e(TAG, "storeGameState()");
        ArrayList<String> gameData = master.getGameState();
        File filePath = context.getExternalFilesDir(null);
        try {
            File file = new File(filePath, GAME_STATE_FILE_NAME);
            FileWriter writer = new FileWriter(file);
            for (String line : gameData) {
                writer.append(line + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> readSavedData() {
        // Read game state data from file
        Log.e(TAG, "readSavedData()");
        File filePath = context.getExternalFilesDir(null);
        File file = new File(filePath, GAME_STATE_FILE_NAME);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
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
        // separate into arraylist
        String rawData = new String(bytes);
        String[] items = rawData.split("\n");
        ArrayList<String> data = new ArrayList<String>(Arrays.asList(items));
        return data;
    }

}