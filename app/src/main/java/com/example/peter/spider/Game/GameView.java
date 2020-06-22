package com.example.peter.spider.Game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
    public static final String PREV_GAME_FILE_NAME = "prev_game.txt";
    private MainThread thread;
    private Context context;
    DisplayMetrics displayMetrics;
    public Master master;
    private Paint textPaint;
    private boolean cardsInMotion;
    private int screenWidth, screenHeight;
    // Coordinates for "buttons"
    private float topMenuY, bottomMenuY, centerX, undoX, hintX;

    public GameView(Context context, int difficulty) {
        // Constructor for new game
        super(context);
        this.context = context;
        Log.e(TAG, "New game constructor");
        HashMap<Integer, Drawable> mStore = initialize(difficulty);
        // Initialize Master
        master = new Master(screenWidth, screenHeight, difficulty, mStore);
        initPaints(master.textPaintSize);
    }

    public GameView(Context context) {
        // Constructor to restore game where it left off.
        super(context);
        this.context = context;
        Log.e(TAG, "Restore game constructor");
        // Get saved data from file
        ArrayList<String> savedData = readSavedData();
        String line = savedData.get(0);
        String[] data = line.split(",");
        int difficulty = Integer.parseInt(data[1]);
        HashMap<Integer, Drawable> mStore = initialize(difficulty);
        // Re-create master
        master = new Master(screenWidth, screenHeight, mStore, savedData);
        initPaints(master.textPaintSize);
    }

    private void updateDisplayMetrics() {
        displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    private HashMap<Integer, Drawable> initialize(int difficulty) {
        // Helper method used by both constructors
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
        // Get display metrics (screen width & height)
        updateDisplayMetrics();
        // Initialize Image HashMap
        HashMap<Integer, Drawable> mStore = new HashMap<Integer, Drawable>();
        Drawable cardBack = getResources().getDrawable(R.drawable.card_back, null);
        mStore.put(R.id.card_back, cardBack);
        Drawable spades = getResources().getDrawable(R.drawable.suit_spades, null);
        mStore.put(R.id.suit_spades, spades);
        // Load more suits depending on difficulty
        if (difficulty > 1) {
            Drawable hearts = getResources().getDrawable(R.drawable.suit_hearts, null);
            mStore.put(R.id.suit_hearts, hearts);
        }
        if (difficulty > 2) {
            Drawable diamonds = getResources().getDrawable(R.drawable.suit_diamonds, null);
            mStore.put(R.id.suit_diamonds, diamonds);
        }
        if (difficulty > 3) {
            Drawable clubs = getResources().getDrawable(R.drawable.suit_clubs, null);
            mStore.put(R.id.suit_clubs, clubs);
        }
        return mStore;
    }

    private void initPaints(int paintSize) {
        textPaint = new Paint();
        textPaint.setColor(Color.rgb(30,30,30));
        textPaint.setTextSize(paintSize);
        // Get Y-coords for top and bottom menu's
        topMenuY = textPaint.getTextSize()+5;
        bottomMenuY = screenHeight-textPaint.getTextSize();
        // Inform stacks how tall they can be
        master.setBottomMenuY(bottomMenuY);
        // Get X-coord for Menu "button
        Rect centerBounds = new Rect();
        String mText = "MENU";
        textPaint.getTextBounds(mText, 0, mText.length(), centerBounds);
        centerX = (float) (screenWidth - centerBounds.width()) / 2;
        // Get X-coord for Undo "button"
        undoX = (float) 0.02 * screenWidth;
        // Get X-coord for hint button
        Rect hintBounds = new Rect();
        mText = "HINT";
        textPaint.getTextBounds(mText, 0, mText.length(), hintBounds);
        hintX = (float) (screenWidth - hintBounds.width() - 15);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cardsInMotion = false;
        if (thread == null) {
            getHolder().addCallback(this);
            thread = new MainThread(getHolder(), this);
        }
        if (!thread.getRunning()) {
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        //Log.e(TAG, "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.e(TAG, "surfaceDestroyed()");
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
            canvas.drawText("Moves: " + moves, 15, topMenuY, textPaint);

            // Draw menu "button"
            canvas.drawText("MENU", centerX, topMenuY, textPaint);

            // Draw score
            String score = String.valueOf(master.historyTracker.getScore());
            // TODO: Get proper size of text
            canvas.drawText(score, (float) (0.8 * screenWidth), topMenuY, textPaint);

            // Draw "undo" button (bottom-left) TODO: Use undo icon instead
            canvas.drawText("UNDO", undoX, bottomMenuY, textPaint);

            // Draw "hint" button (bottom-right)
            canvas.drawText("HINT", hintX, bottomMenuY, textPaint);

            // Draw time elapsed (bottom center [directly below menu text])
            String time = master.historyTracker.getTimeElapsed();
            canvas.drawText(time, centerX, bottomMenuY, textPaint);

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
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Initial touch of screen, check legality
                cardsInMotion = master.legalTouch(x, y);
                if ((!master.historyTracker.isClockRunning()) && cardsInMotion) {
                    master.historyTracker.startClock();
                }
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
                if (cardsInMotion) {
                    // Lock into place if legal move, else go back to initial location
                    boolean gameOver = master.endStackMotion(x, y);
                    cardsInMotion = false;
                    if (gameOver) {
                        gameWon();
                    }
                } else {
                    // TODO: Use actual buttons instead of just text?
                    int trueCenterX = screenWidth / 2;
                    int menuLeft = (int) centerX;
                    int menuRight = ((int) (trueCenterX - centerX) * 2) + menuLeft;
                    // Check if undo "button" was touched
                    // TODO: Improve the 0.95 for comfortable pressing of UNDO
                    if ((x < (0.25*screenWidth)) && (y > (0.95 * bottomMenuY))) {
                        if (!master.undo()) {
                            Toast.makeText(context, "Nothing to undo...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Check if menu "button" was touched
                    else if ((y < topMenuY) && (x > menuLeft) && (x < menuRight)) {
                        // Return to home screen
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);
                    }
                    // Check if show cards arrow touched
                    else if (master.arrowTouched(x, y)) {
                        Log.e(TAG, "Arrow touched!");
                    }
                    // Check if hint "button" was touched
                    else if ((x > hintX) && (y > (0.95 * bottomMenuY))) {
                        String msg = master.showHint();
                        if (msg != null) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    // TODO: Remove automatic win
                    else if ((x > (0.25*screenWidth)) && (x < (0.75*screenWidth)) && (y > (0.95 * bottomMenuY))) {
                        gameWon();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
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
        int textPaintSize = master.updateOrientation(screenWidth, screenHeight);
        // Re-size menu's
        initPaints(textPaintSize);
    }

    public void update() {
        // This is called continuously right before draw() is called
    }

    private void gameWon() {
        // Called when the game is won
        Toast.makeText(context, "You Win!", Toast.LENGTH_SHORT).show();
        // Store finished game state to PREV_GAME_FILE_NAME
        storeGameState(PREV_GAME_FILE_NAME);
        // Delete saved data in GAME_STATE_FILE_NAME
        File filePath = context.getExternalFilesDir(null);
        try {
            File file = new File(filePath, GAME_STATE_FILE_NAME);
            //boolean deleted = file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Get stats, add to intent & launch game won screen
        String currentTime = master.historyTracker.getTimeElapsed();
        int currentMoves = master.historyTracker.getNumMoves();
        int currentScore = master.historyTracker.getScore();
        Intent i = new Intent(context, StatsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("difficulty", master.difficulty);
        i.putExtra("currentTime", currentTime);
        i.putExtra("currentMoves", currentMoves);
        i.putExtra("currentScore", currentScore);
        // TODO: Trying to fix activity stack
        killThread();
        context.startActivity(i);
        Log.e(TAG, "Killing activity");
    }

    public void storeGameState() {
        /*
        Called from SelectDifficultyActivity.onPause(), stores game state data to file.
        Default value: GAME_STATE_FILE_NAME
         */
        storeGameState(GAME_STATE_FILE_NAME);
    }

    public void storeGameState(String path) {
        /*
        Stores the game state to specified location.
        Either: GAME_STATE_FILE_NAME if called from SelectDifficultyActivity.onPause();
        Or: PREV_GAME_FILE_NAME when the game is won.
         */
        ArrayList<String> gameData = master.getGameState();
        File filePath = context.getExternalFilesDir(null);
        try {
            File file = new File(filePath, path);
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
            //Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            //Log.e("login activity", "Can not read file: " + e.toString());
        }
        // separate into arraylist
        String rawData = new String(bytes);
        String[] items = rawData.split("\n");
        ArrayList<String> data = new ArrayList<String>(Arrays.asList(items));
        return data;
    }

}