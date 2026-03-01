package com.readyrecipe.backend.controller;

import com.readyrecipe.backend.entity.GroceryItem;
import com.readyrecipe.backend.entity.PantryItem;
import com.readyrecipe.backend.repository.GroceryRepository;
import com.readyrecipe.backend.repository.PantryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grocery")
@CrossOrigin(origins = "*")
public class GroceryController {

    @Autowired
    private GroceryRepository groceryRepository;

    @Autowired
    private PantryRepository pantryRepository;

    @GetMapping("")
    public ResponseEntity<List<GroceryItem>> listItems(@RequestParam(name = "userId") String userId) {
        try {
            UUID uid = UUID.fromString(userId);
            return ResponseEntity.ok(groceryRepository.findByUserId(uid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<GroceryItem> addItem(@RequestBody GroceryItem request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        GroceryItem item = new GroceryItem();
        item.setUserId(request.getUserId());
        item.setName(request.getName().trim());
        item.setChecked(false);
        item.setCreatedAt(Instant.now());

        return ResponseEntity.ok(groceryRepository.save(item));
    }

    @PatchMapping("/{id}/checkoff")
    public ResponseEntity<GroceryItem> checkoff(@PathVariable("id") String id, @RequestBody(required = false) GroceryItem request) {
        UUID uid;
        try {
            uid = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        Optional<GroceryItem> optionalItem = groceryRepository.findById(uid);
        if (optionalItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GroceryItem item = optionalItem.get();
        item.setChecked(true);
        groceryRepository.save(item);

        PantryItem pantryItem = new PantryItem();
        pantryItem.setUserId(item.getUserId());
        pantryItem.setItemName(item.getName());
        pantryItem.setQuantity(BigDecimal.ONE);
        pantryItem.setUnit("unit");
        pantryItem.setCategory("grocery");
        pantryItem.setExpiryDate(calculateExpiry());
        pantryRepository.save(pantryItem);

        return ResponseEntity.ok(item);
    }

    private LocalDate calculateExpiry() {
        return LocalDate.now().plusDays(7);
    }
}