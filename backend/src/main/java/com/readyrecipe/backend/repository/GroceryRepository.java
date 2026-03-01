package com.readyrecipe.backend.repository;

import com.readyrecipe.backend.entity.GroceryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroceryRepository extends JpaRepository<GroceryItem, UUID> {
    List<GroceryItem> findByUserId(UUID userId);
}