package com.readyrecipe.backend.dto;

import java.math.BigDecimal;


public class PantryItemDTO {
    private String name;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private String category;
    // expiryDate as ISO string (yyyy-MM-dd)
    private String expiryDate;
    private String userId;

    public String getName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return itemName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}