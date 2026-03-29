package com.example.memoryflipgame;

public class CardModel {
    private String emoji;
    private boolean isFaceUp;
    private boolean isMatched;
    private int id;

    public CardModel(int id, String emoji) {
        this.id = id;
        this.emoji = emoji;
        this.isFaceUp = false;
        this.isMatched = false;
    }

    public int getId()          { return id; }
    public String getEmoji()    { return emoji; }
    public boolean isFaceUp()   { return isFaceUp; }
    public boolean isMatched()  { return isMatched; }

    public void setFaceUp(boolean faceUp)   { this.isFaceUp = faceUp; }
    public void setMatched(boolean matched) {
        this.isMatched = matched;
        if (matched) this.isFaceUp = true;
    }
}