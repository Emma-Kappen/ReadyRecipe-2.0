package com.readyrecipe.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.Recipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeSuggestionAdapter extends RecyclerView.Adapter<RecipeSuggestionAdapter.RecipeViewHolder> {
    public interface OnCookClickListener {
        void onCook(Recipe recipe);
    }

    private List<Recipe> recipes = new ArrayList<>();
    private final OnCookClickListener cookClickListener;

    public RecipeSuggestionAdapter(OnCookClickListener cookClickListener) {
        this.cookClickListener = cookClickListener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_suggestion, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.title.setText(recipe.getName());
        String cuisine = recipe.getCuisineType() == null || recipe.getCuisineType().isEmpty()
                ? "Cuisine"
                : recipe.getCuisineType();
        String meta = String.format(Locale.getDefault(), "%s • %d min", cuisine, recipe.getCookingTime());
        holder.meta.setText(meta);
        holder.btnCook.setOnClickListener(v -> {
            if (cookClickListener != null) {
                cookClickListener.onCook(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        Button btnCook;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvRecipeTitle);
            meta = itemView.findViewById(R.id.tvRecipeMeta);
            btnCook = itemView.findViewById(R.id.btnCook);
        }
    }
}
