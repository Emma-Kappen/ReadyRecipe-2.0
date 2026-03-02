package com.readyrecipe.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.PantryItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    public interface OnPantryItemActionListener {
        void onEdit(PantryItem item);
        void onLongPress(PantryItem item, int position);
    }

    private List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemActionListener actionListener;

    public PantryAdapter() {
        this(null);
    }

    public PantryAdapter(OnPantryItemActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setItems(List<PantryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public PantryItem getItem(int position) {
        return (position >= 0 && position < items.size()) ? items.get(position) : null;
    }

    public void removeAt(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addItem(PantryItem item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        ensureSquareCard(holder.itemView);

        holder.name.setText(item.getItemName());

        String qtyText = "";
        if (item.getQuantity() != null) {
            BigDecimal qty = item.getQuantity();
            qtyText = qty.stripTrailingZeros().toPlainString();
        }
        if (item.getUnit() != null) {
            qtyText = qtyText + " " + item.getUnit();
        }
        holder.quantity.setText(qtyText.trim());

        String meta = item.getCategory() != null ? item.getCategory() : "";
        holder.meta.setText(String.format(Locale.getDefault(), "Category: %s", meta));

        String expiryText = item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()
                ? String.format(Locale.getDefault(), "Expires: %s", item.getExpiryDate())
                : "Expiry: auto";
        holder.expiry.setText(expiryText);

        holder.editButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(item);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    actionListener.onLongPress(item, adapterPosition);
                    return true;
                }
            }
            return false;
        });
    }

    private void ensureSquareCard(View itemView) {
        itemView.post(() -> {
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            int width = itemView.getWidth();
            if (params != null && width > 0 && params.height != width) {
                params.height = width;
                itemView.setLayoutParams(params);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView quantity;
        TextView meta;
        TextView expiry;
        ImageButton editButton;

        public PantryViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            quantity = itemView.findViewById(R.id.tvQuantity);
            meta = itemView.findViewById(R.id.tvMeta);
            expiry = itemView.findViewById(R.id.tvExpiry);
            editButton = itemView.findViewById(R.id.btnEditPantry);
        }
    }
}
