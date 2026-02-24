package com.readyrecipe.backend.dto;

public class DashboardStatsDTO {
    private long totalItems;
    private long expiringSoonCount;
    private long recipesSaved;
    private double foodSavingsPercent;

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public long getExpiringSoonCount() { return expiringSoonCount; }
    public void setExpiringSoonCount(long expiringSoonCount) { this.expiringSoonCount = expiringSoonCount; }

    public long getRecipesSaved() { return recipesSaved; }
    public void setRecipesSaved(long recipesSaved) { this.recipesSaved = recipesSaved; }

    public double getFoodSavingsPercent() { return foodSavingsPercent; }
    public void setFoodSavingsPercent(double foodSavingsPercent) { this.foodSavingsPercent = foodSavingsPercent; }
}
