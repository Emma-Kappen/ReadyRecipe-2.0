package com.readyrecipe.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String cuisineType;
    private Integer cookingTime; // minutes
    private Double rating;
    private String imageUrl;

    public Recipe() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public Integer getCookingTime() { return cookingTime; }
    public void setCookingTime(Integer cookingTime) { this.cookingTime = cookingTime; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
