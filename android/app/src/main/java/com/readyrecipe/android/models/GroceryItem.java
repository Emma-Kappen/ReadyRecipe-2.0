package com.readyrecipe.android.models;

import java.math.BigDecimal;
import java.util.UUID;

public class GroceryItem {
    private UUID id;
    private UUID userId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private String category;
    private String priority; // e.g., high/low
    private boolean checked;

    public GroceryItem() {}

    public GroceryItem(UUID id, UUID userId, String name, BigDecimal quantity, String unit, String category, String priority, boolean checked) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.priority = priority;
        this.checked = checked;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
