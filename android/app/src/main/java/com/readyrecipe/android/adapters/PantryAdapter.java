package com.readyrecipe.android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.PantryItem;
import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    private List<PantryItem> items = new ArrayList<>();

    public PantryAdapter() {}

    public void setItems(List<PantryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        // Use simple_list_item_2 which has text1 and text2
        holder.text1.setText(item.getItemName());
        String detail = item.getQuantity() + " " + item.getUnit() + " | " + item.getCategory();
        if (item.getExpiryDate() != null) {
            detail += " | Expires: " + item.getExpiryDate();
        }
        holder.text2.setText(detail);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        public TextView text1;
        public TextView text2;

        public PantryViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
