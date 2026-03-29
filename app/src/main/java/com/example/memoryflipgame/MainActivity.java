package com.example.memoryflipgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MODE       = "mode";
    public static final String MODE_CLASSIC     = "classic";
    public static final String MODE_CHALLENGE   = "challenge";
    public static final String EXTRA_MOVE_LIMIT = "move_limit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnClassic = findViewById(R.id.btnClassic);
        Button btnEasy    = findViewById(R.id.btnEasy);
        Button btnMedium  = findViewById(R.id.btnMedium);
        Button btnHard    = findViewById(R.id.btnHard);

        btnClassic.setOnClickListener(v -> startGame(MODE_CLASSIC, 0,8));
        btnEasy   .setOnClickListener(v -> startGame(MODE_CHALLENGE, 8, 4));
        btnMedium .setOnClickListener(v -> startGame(MODE_CHALLENGE, 10, 6));
        btnHard   .setOnClickListener(v -> startGame(MODE_CHALLENGE, 12, 8));
    }

    private void startGame(String mode, int moveLimit, int pairCount) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_MOVE_LIMIT, moveLimit);
        intent.putExtra("pair_count", pairCount);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}