package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * JPA repository for Item entities.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Retrieves all Item IDs (for batch processing).
     * @return list of all item IDs
     */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}