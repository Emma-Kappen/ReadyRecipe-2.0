package com.readyrecipe.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.readyrecipe.backend.entity.PantryItem;
import com.readyrecipe.backend.repository.PantryRepository;

import com.readyrecipe.backend.dto.PantryItemDTO;
import com.readyrecipe.backend.dto.DashboardStatsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/pantry")
@CrossOrigin(origins = "*")
public class PantryController {
    
    @Autowired
    private PantryRepository pantryRepository;

    @PostMapping("")
    public ResponseEntity<PantryItem> addPantryItem(@RequestBody PantryItem item) {
        PantryItem saved = mapAndSavePantryItem(item);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PantryItem>> addPantryItems(@RequestBody List<PantryItemDTO> items) {
        List<PantryItem> savedItems = new ArrayList<>();
        if (items != null) {
            for (PantryItemDTO item : items) {
                if (item == null || item.getName() == null || item.getName().isBlank()) {
                    continue;
                }
                PantryItem mapped = new PantryItem();
                mapped.setItemName(item.getName().trim());
                mapped.setQuantity(item.getQuantity());
                mapped.setUnit(item.getUnit());
                mapped.setCategory(item.getCategory());
                if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
                    try {
                        mapped.setExpiryDate(LocalDate.parse(item.getExpiryDate()));
                    } catch (Exception ignored) {}
                }
                if (item.getUserId() != null && !item.getUserId().isEmpty()) {
                    try {
                        mapped.setUserId(UUID.fromString(item.getUserId()));
                    } catch (Exception ignored) {}
                }
                savedItems.add(mapAndSavePantryItem(mapped));
            }
        }
        return ResponseEntity.ok(savedItems);
    }
    
    @PostMapping("/add")
    public ResponseEntity<String> addItem(@RequestBody PantryItemDTO item) {
        PantryItem entity = new PantryItem();
        entity.setItemName(item.getName());
        entity.setQuantity(item.getQuantity());
        entity.setUnit(item.getUnit());
        entity.setCategory(item.getCategory());
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            try {
                entity.setExpiryDate(LocalDate.parse(item.getExpiryDate()));
            } catch (Exception e) {}
        }
        if (item.getUserId() != null && !item.getUserId().isEmpty()) {
            try { entity.setUserId(UUID.fromString(item.getUserId())); } catch (Exception e) {}
        }
        pantryRepository.save(entity);
        return ResponseEntity.ok("Item added: " + item.getName());
    }

    private PantryItem mapAndSavePantryItem(PantryItem item) {
        PantryItem entity = new PantryItem();
        if (item != null) {
            entity.setUserId(item.getUserId());
            entity.setItemName(item.getItemName());
            entity.setQuantity(item.getQuantity());
            entity.setUnit(item.getUnit());
            entity.setCategory(item.getCategory());
            if (item.getExpiryDate() != null) {
                entity.setExpiryDate(item.getExpiryDate());
            }
        }
        if (entity.getItemName() == null || entity.getItemName().isBlank()) {
            entity.setItemName("Unknown Item");
        }
        if (entity.getQuantity() == null) {
            entity.setQuantity(java.math.BigDecimal.ONE);
        }
        if (entity.getUnit() == null || entity.getUnit().isBlank()) {
            entity.setUnit("unit");
        }
        if (entity.getCategory() == null || entity.getCategory().isBlank()) {
            entity.setCategory("grocery");
        }
        return pantryRepository.save(entity);
    }

    @GetMapping("")
    public ResponseEntity<List<PantryItem>> listItems(@RequestParam(name = "userId") String userId) {
        try {
            UUID uid = UUID.fromString(userId);
            List<PantryItem> items = pantryRepository.findByUserId(uid);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> stats(@RequestParam(name = "userId") String userId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        try {
            UUID uid = UUID.fromString(userId);
            List<PantryItem> items = pantryRepository.findByUserId(uid);
            stats.setTotalItems(items.size());
            long expiring = items.stream().filter(i -> i.getExpiryDate() != null)
                    .filter(i -> {
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), i.getExpiryDate());
                        return days >= 0 && days <= 7;
                    }).count();
            stats.setExpiringSoonCount(expiring);
            // Placeholder values for recipesSaved and foodSavingsPercent
            stats.setRecipesSaved(0);
            stats.setFoodSavingsPercent(0.0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
