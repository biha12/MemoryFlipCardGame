package com.example.memoryflipgame;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardModel> cardList;
    private Context context;
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    public CardAdapter(Context context, List<CardModel> cardList, OnCardClickListener listener) {
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        // Make cards square based on parent width
        int columns = 4;
        int padding = (int) (context.getResources().getDisplayMetrics().density * 24);
        int size = (parent.getMeasuredWidth() - padding) / columns;
        view.setLayoutParams(new RecyclerView.LayoutParams(size, size));
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardModel card = cardList.get(position);
        holder.tvEmoji.setText(card.getEmoji());

        if (card.isMatched()) {
            holder.cardBack.setVisibility(View.GONE);
            holder.cardFront.setVisibility(View.VISIBLE);
            holder.cardFront.setBackgroundResource(R.drawable.card_matched);
            holder.itemView.setAlpha(0.85f);
            holder.itemView.setClickable(false);
        } else if (card.isFaceUp()) {
            holder.cardBack.setVisibility(View.GONE);
            holder.cardFront.setVisibility(View.VISIBLE);
            holder.cardFront.setBackgroundResource(R.drawable.card_front);
            holder.itemView.setClickable(false);
        } else {
            holder.cardBack.setVisibility(View.VISIBLE);
            holder.cardFront.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCardClick(holder.getAdapterPosition());
            });
        }
    }

    public void flipCard(int position, boolean faceUp, Runnable onComplete) {
        View view = null;
        // We animate directly on the ViewHolder's itemView via notifyItemChanged
        // Simple approach: update model and notify, let RecyclerView re-bind
        cardList.get(position).setFaceUp(faceUp);
        notifyItemChanged(position);
        if (onComplete != null) onComplete.run();
    }

    @Override
    public int getItemCount() { return cardList.size(); }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardBack, cardFront;
        TextView tvEmoji;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBack  = itemView.findViewById(R.id.cardBack);
            cardFront = itemView.findViewById(R.id.cardFront);
            tvEmoji   = itemView.findViewById(R.id.tvEmoji);
        }
    }
}