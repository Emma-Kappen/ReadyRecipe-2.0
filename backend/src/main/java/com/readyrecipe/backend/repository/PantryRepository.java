package com.readyrecipe.backend.repository;
import com.readyrecipe.backend.entity.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PantryRepository extends JpaRepository<PantryItem, UUID> {
    List<PantryItem> findByUserId(UUID userId);
}
