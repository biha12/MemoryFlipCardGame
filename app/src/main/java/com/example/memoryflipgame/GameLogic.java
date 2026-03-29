package com.example.memoryflipgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {

    private static final String[] EMOJIS = {
            "🌸", "🦋", "🌺", "💎", "🌙", "⭐", "🎀", "🍓"
    };

    public static List<CardModel> generateCards(int pairCount) {
        List<CardModel> cards = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < pairCount; i++) {
            cards.add(new CardModel(id++, EMOJIS[i]));
            cards.add(new CardModel(id++, EMOJIS[i]));
        }
        Collections.shuffle(cards);
        return cards;
    }

    public static boolean isMatch(CardModel a, CardModel b) {
        return a.getEmoji().equals(b.getEmoji());
    }
}