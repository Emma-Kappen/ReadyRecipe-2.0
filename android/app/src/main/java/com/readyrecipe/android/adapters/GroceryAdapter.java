package com.readyrecipe.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.GroceryItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {
    public interface OnCheckedChangeListener {
        void onCheckedChanged(GroceryItem item, boolean isChecked);
    }

    private List<GroceryItem> items = new ArrayList<>();
    private final OnCheckedChangeListener listener;

    public GroceryAdapter(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GroceryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addItem(GroceryItem item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grocery, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isChecked());
        holder.checkBox.setText(item.getName());

        String qty = item.getQuantity() != null ? item.getQuantity().stripTrailingZeros().toPlainString() : "1";
        String unit = item.getUnit() != null ? item.getUnit() : "unit";
        String priority = item.getPriority() != null ? item.getPriority() : "normal";
        holder.meta.setText(String.format(Locale.getDefault(), "%s %s • %s", qty, unit, priority));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCheckedChanged(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GroceryViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView meta;

        GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkItem);
            meta = itemView.findViewById(R.id.tvGroceryMeta);
        }
    }
}
