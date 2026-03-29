package com.example.memoryflipgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.media.MediaPlayer;

public class GameActivity extends AppCompatActivity implements CardAdapter.OnCardClickListener {

    MediaPlayer clickSound, winSound, loseSound;
    private int totalPairs = 8;
    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardModel> cardList;
    private TextView tvMoves, tvPairs, tvMovesLeft, tvTitle;
    private LinearLayout challengeBar;

    private int firstIndex  = -1;
    private int secondIndex = -1;
    private boolean canFlip = true;
    private int moveCount   = 0;
    private int pairsFound  = 0;
    private int TOTAL_PAIRS = 8;

    // Challenge mode fields
    private boolean isChallengeMode = false;
    private int moveLimit = 0;
    private int movesLeft = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        clickSound = MediaPlayer.create(this, R.raw.click);
        winSound   = MediaPlayer.create(this, R.raw.win);
        loseSound  = MediaPlayer.create(this, R.raw.lose);

        tvMoves      = findViewById(R.id.tvMoves);
        tvPairs      = findViewById(R.id.tvPairs);
        tvMovesLeft  = findViewById(R.id.tvMovesLeft);
        tvTitle      = findViewById(R.id.tvTitle);
        challengeBar = findViewById(R.id.challengeBar);

        // Read mode from intent
        String mode = getIntent().getStringExtra(MainActivity.EXTRA_MODE);
        moveLimit   = getIntent().getIntExtra(MainActivity.EXTRA_MOVE_LIMIT, 0);
        totalPairs = getIntent().getIntExtra("pair_count", 8);
        TOTAL_PAIRS = totalPairs;

        isChallengeMode = MainActivity.MODE_CHALLENGE.equals(mode);

        if (isChallengeMode) {
            challengeBar.setVisibility(View.VISIBLE);
            movesLeft = moveLimit;
            tvTitle.setText("Challenge");
            updateMovesLeft();
        }

        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> resetGame());
        setupGame();
    }

    private void setupGame() {
        cardList = GameLogic.generateCards(totalPairs);
        adapter  = new CardAdapter(this, cardList, this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(adapter);
        updateStats();
    }

    private void resetGame() {
        firstIndex  = -1;
        secondIndex = -1;
        canFlip     = true;
        moveCount   = 0;
        pairsFound  = 0;
        if (isChallengeMode) {
            movesLeft = moveLimit;
            updateMovesLeft();
        }
        setupGame();
    }

    @Override
    public void onCardClick(int position) {

        if (clickSound != null) clickSound.start();
        if (!canFlip) return;
        if (position == firstIndex) return;
        CardModel card = cardList.get(position);
        if (card.isMatched() || card.isFaceUp()) return;

        flipCardWithAnimation(position, true, () -> {
            if (firstIndex == -1) {
                firstIndex = position;
            } else {
                secondIndex = position;
                canFlip = false;
                moveCount++;

                // Deduct from challenge moves
                if (isChallengeMode) {
                    movesLeft--;
                    updateMovesLeft();
                    // Turn bar red when low
                    if (movesLeft <= 3) {
                        challengeBar.setBackgroundColor(0xFFB71C1C);
                    } else if (movesLeft <= 6) {
                        challengeBar.setBackgroundColor(0xFFE91E63);
                    }
                }

                updateStats();
                checkMatch();
            }
        });
    }

    private void flipCardWithAnimation(int position, boolean faceUp, Runnable onComplete) {
        RecyclerView.ViewHolder vh =
                recyclerView.findViewHolderForAdapterPosition(position);
        View itemView = vh != null ? vh.itemView : null;

        if (itemView == null) {
            cardList.get(position).setFaceUp(faceUp);
            adapter.notifyItemChanged(position);
            if (onComplete != null) onComplete.run();
            return;
        }

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(itemView, "rotationY", 0f, 90f);
        flipOut.setDuration(150);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) {
                cardList.get(position).setFaceUp(faceUp);
                adapter.notifyItemChanged(position);
                ObjectAnimator flipIn = ObjectAnimator.ofFloat(itemView, "rotationY", -90f, 0f);
                flipIn.setDuration(150);
                flipIn.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator a) {
                        if (onComplete != null) onComplete.run();
                    }
                });
                flipIn.start();
            }
        });
        flipOut.start();
    }

    private void checkMatch() {
        CardModel first  = cardList.get(firstIndex);
        CardModel second = cardList.get(secondIndex);

        if (GameLogic.isMatch(first, second)) {
            if (winSound != null) winSound.start();
            first.setMatched(true);
            second.setMatched(true);
            adapter.notifyItemChanged(firstIndex);
            adapter.notifyItemChanged(secondIndex);
            pairsFound++;
            updateStats();
            resetSelection();
            canFlip = true;

            if (pairsFound == TOTAL_PAIRS) {
                new Handler(Looper.getMainLooper())
                        .postDelayed(() -> showResultDialog(true), 400);
            }
        } else {
            if (clickSound != null) clickSound.start();
            int savedFirst  = firstIndex;
            int savedSecond = secondIndex;
            resetSelection();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                flipCardWithAnimation(savedFirst,  false, null);
                flipCardWithAnimation(savedSecond, false, () -> {
                    canFlip = true;
                    // Challenge: check if out of moves after flip-back
                    if (isChallengeMode && movesLeft <= 0 && pairsFound < TOTAL_PAIRS) {
                        new Handler(Looper.getMainLooper())
                                .postDelayed(() -> showResultDialog(false), 300);
                    }
                });
            }, 800);
        }
    }

    private void resetSelection() {
        if (clickSound != null) clickSound.start();
        firstIndex  = -1;
        secondIndex = -1;
    }

    private void updateStats() {
        tvMoves.setText(getString(R.string.moves, moveCount));
        tvPairs.setText("Pairs: " + pairsFound + "/" + TOTAL_PAIRS);
    }

    private void updateMovesLeft() {
        tvMovesLeft.setText("Moves Left: " + movesLeft);
    }

    private void showResultDialog(boolean won) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_win);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvIcon  = dialog.findViewById(R.id.tvDialogIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMsg   = dialog.findViewById(R.id.tvWinMessage);

        if (won) {
            if (winSound != null) winSound.start();
            tvIcon.setText("🏆");
            tvTitle.setText("You Win!");
            if (isChallengeMode) {
                tvMsg.setText("Challenge beaten with " + movesLeft + " moves to spare!");
            } else {
                tvMsg.setText("Completed in " + moveCount + " moves!");
            }
        } else {
            if (loseSound != null) loseSound.start();
            tvIcon.setText("💔");
            tvTitle.setText("Out of Moves!");
            tvMsg.setText("You found " + pairsFound + " / " + TOTAL_PAIRS
                    + " pairs.\nTry again!");
        }

        Button btnPlayAgain = dialog.findViewById(R.id.btnPlayAgain);
        Button btnMenu      = dialog.findViewById(R.id.btnMenu);

        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
        });
        btnMenu.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        dialog.show();
    }
}