package com.baogame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;

/**
 * The Simple implementation of a traditional African board game.
 * Author: Macharia Barii
 */
public class MainActivity extends AppCompatActivity {
    private final int[] pits = new int[16]; // 16 pits (8 for each player)
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean player1Turn = true;
    private final Handler handler = new Handler();
    private MediaPlayer seedMoveSound;
    private MediaPlayer captureSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeGame();
        setupPitClickListeners();
        setupRestartButton();
        setupHelpButton();
        loadSounds();
        updateUI();
    }
    private void initializeGame() {
        // Each pit starts with 4 seeds
        Arrays.fill(pits, 4);
        player1Score = 0;
        player2Score = 0;
        player1Turn = true;
    }

    private void setupPitClickListeners() {
        GridLayout gameBoard = findViewById(R.id.gameBoard);
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button pitButton = (Button) gameBoard.getChildAt(i);
            pitButton.setOnClickListener(this::onPitClick);
        }
    }

    private void setupRestartButton() {
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> {
            initializeGame();
            updateUI();
        });
    }

    private void setupHelpButton() {
        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
        });
    }

    private void loadSounds() {
        seedMoveSound = MediaPlayer.create(this, R.raw.seed_move);
        captureSound = MediaPlayer.create(this, R.raw.capture);
    }

    private void onPitClick(View v) {
        Button clickedPit = (Button) v;
        int pitIndex = Integer.parseInt(clickedPit.getTag().toString()) - 1;

        if (isValidMove(pitIndex)) {
            sowSeedsWithAnimation(pitIndex);
        }
    }

    private boolean isValidMove(int pitIndex) {
        if (player1Turn) {
            return pitIndex >= 8 && pitIndex < 16 && pits[pitIndex] > 0;
        } else {
            return pitIndex >= 0 && pitIndex < 8 && pits[pitIndex] > 0;
        }
    }

    private void sowSeedsWithAnimation(int pitIndex) {
        int seeds = pits[pitIndex];
        pits[pitIndex] = 0;
        updateUI();

        handler.postDelayed(new Runnable() {
            int currentSeed = 1;
            int currentPit = pitIndex;

            @Override
            public void run() {
                if (currentSeed <= seeds) {
                    currentPit = (currentPit + 1) % pits.length;
                    pits[currentPit]++;
                    updateUI();
                    seedMoveSound.start(); // Play sound effect
                    currentSeed++;

                    if (currentSeed <= seeds) {
                        handler.postDelayed(this, 500); // Delay for animation
                    } else {
                        checkForCapture(currentPit);
                        checkGameOver();
                        switchPlayer();
                    }
                }
            }
        }, 500); // Initial delay
    }

    private void checkForCapture(int pitIndex) {
        while (pits[pitIndex] > 1 && pits[pitIndex] <= 3) {
            int oppositePitIndex = 15 - pitIndex;
            if (pits[oppositePitIndex] > 0) {
                if (player1Turn) {
                    player1Score += pits[oppositePitIndex] + pits[pitIndex];
                } else {
                    player2Score += pits[oppositePitIndex] + pits[pitIndex];
                }
                pits[oppositePitIndex] = 0;
                pits[pitIndex] = 0;
                updateUI();
                captureSound.start(); // Play sound effect
                pitIndex = (pitIndex - 1 + pits.length) % pits.length; // Move to previous pit
            } else {
                break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        GridLayout gameBoard = findViewById(R.id.gameBoard);
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button pitButton = (Button) gameBoard.getChildAt(i);
            pitButton.setText(String.valueOf(pits[i]));
        }

        TextView player1ScoreView = findViewById(R.id.player1Score);
        TextView player2ScoreView = findViewById(R.id.player2Score);
        TextView turnIndicator = findViewById(R.id.turnIndicator);

        player1ScoreView.setText("Player 1: " + player1Score);
        player2ScoreView.setText("Player 2: " + player2Score);
        turnIndicator.setText(player1Turn ? "Player 1's Turn" : "Player 2's Turn");
    }

    private void switchPlayer() {
        player1Turn = !player1Turn;
    }

    private void checkGameOver() {
        boolean player1Empty = true;
        boolean player2Empty = true;

        for (int i = 8; i < 16; i++) {
            if (pits[i] > 0) {
                player1Empty = false;
                break;
            }
        }

        for (int i = 0; i < 8; i++) {
            if (pits[i] > 0) {
                player2Empty = false;
                break;
            }
        }

        if (player1Empty || player2Empty) {
            endGame();
        }
    }

    @SuppressLint("SetTextI18n")
    private void endGame() {
        TextView turnIndicator = findViewById(R.id.turnIndicator);
        if (player1Score > player2Score) {
            turnIndicator.setText("Player 1 Wins!");
        } else if (player2Score > player1Score) {
            turnIndicator.setText("Player 2 Wins!");
        } else {
            turnIndicator.setText("It's a Tie!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seedMoveSound != null) {
            seedMoveSound.release();
        }
        if (captureSound != null) {
            captureSound.release();
        }
    }
}
