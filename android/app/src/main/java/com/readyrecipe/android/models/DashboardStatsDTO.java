package com.readyrecipe.android.models;

public class DashboardStatsDTO {
    private int totalItems;
    private int expiringSoonCount;
    private int recipesSaved;
    private double foodSavingsPercent;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(int totalItems, int expiringSoonCount, int recipesSaved, double foodSavingsPercent) {
        this.totalItems = totalItems;
        this.expiringSoonCount = expiringSoonCount;
        this.recipesSaved = recipesSaved;
        this.foodSavingsPercent = foodSavingsPercent;
    }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getExpiringSoonCount() { return expiringSoonCount; }
    public void setExpiringSoonCount(int expiringSoonCount) { this.expiringSoonCount = expiringSoonCount; }

    public int getRecipesSaved() { return recipesSaved; }
    public void setRecipesSaved(int recipesSaved) { this.recipesSaved = recipesSaved; }

    public double getFoodSavingsPercent() { return foodSavingsPercent; }
    public void setFoodSavingsPercent(double foodSavingsPercent) { this.foodSavingsPercent = foodSavingsPercent; }
}
