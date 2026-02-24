package com.readyrecipe.android.models;

import java.math.BigDecimal;
import java.util.UUID;

public class PantryItem {
    private UUID id;
    private UUID userId;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private String category;
    private String expiryDate; // ISO 8601 date string
    private String dateAdded;

    public PantryItem() {}

    public PantryItem(UUID id, UUID userId, String itemName, BigDecimal quantity, String unit, String category, String expiryDate, String dateAdded) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expiryDate = expiryDate;
        this.dateAdded = dateAdded;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
}
