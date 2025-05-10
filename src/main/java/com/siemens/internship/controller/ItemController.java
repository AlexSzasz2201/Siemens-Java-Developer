package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * REST API endpoints for managing Item resources.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    private static final Validator LOCAL_VALIDATOR;
    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        LOCAL_VALIDATOR = factory.getValidator();
    }

    /**
     * GET  /api/items
     * @return OK (200) and list of items
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.findAll());
    }

    /**
     * POST  /api/items
     * @param item - payload to create
     * @return CREATED (201) and created item, or BAD_REQUEST (400) if invalid
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {

        Set<ConstraintViolation<Item>> violations = LOCAL_VALIDATOR.validate(item);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Item savedItem = itemService.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    /**
     * GET  /api/items/{id}
     * @param id - item identifier
     * @return OK (200) and item, or NOT_FOUND (404) if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Item item = itemService.findByIdOrThrow(id);
        return ResponseEntity.ok(item);
    }

    /**
     * PUT  /api/items/{id}
     * @param id - item to update
     * @param item - new data (validated)
     * @return OK (200) and updated item, or NOT_FOUND (404)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @RequestBody Item item) {

        Set<ConstraintViolation<Item>> violations = LOCAL_VALIDATOR.validate(item);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Item updatedItem = itemService.updateItem(id, item);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * DELETE /api/items/{id}
     * @param id - item to remove
     * @return NO_CONTENT (204) on success, NOT_FOUND (404) if missing
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * GET  /api/items/process
     * Trigger asynchronous processing of all items.
     * @return ACCEPTED (202) and list of items already queued
     */
    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(list -> ResponseEntity.accepted().body(list));
    }
}