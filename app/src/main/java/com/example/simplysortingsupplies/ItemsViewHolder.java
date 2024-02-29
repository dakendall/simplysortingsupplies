package com.example.simplysortingsupplies;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemsViewHolder extends RecyclerView.ViewHolder {

    CircleImageView itemImage;
    TextView itemDisplayName, itemDisplayAmount;

    public ItemsViewHolder(@NonNull View itemView) {
        super(itemView);

        itemImage = itemView.findViewById(R.id.itemImage);
        itemDisplayName = itemView.findViewById(R.id.itemDisplayName);
        itemDisplayAmount = itemView.findViewById(R.id.itemDisplayAmount);

    }
}
