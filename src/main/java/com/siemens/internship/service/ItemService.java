package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service layer for Item operations.
 * Abstracts repository access and business rules.
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    /**
     * Shared thread pool for async tasks.
     */
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Holds items processed in current batch.
     */
    private List<Item> processedItems = new ArrayList<>();

    /**
     * Count of items processed so far.
     */
    private int processedCount = 0;

    /**
     * Retrieve all items from the database.
     *
     * @return list of all Item entities
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Get an item based on its id or throw ResourceNotFoundException if not present.
     *
     * @param id - item ID
     * @return found Item
     * @throws ResourceNotFoundException if missing
     */
    public Item findByIdOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
    }

    /**
     * Optional fetch by ID (no exception).
     *
     * @param id - item ID
     * @return Optional<Item>
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Save a new item.
     *
     * @param item - Item that is being written
     * @return the Item that has been written
     */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Delete an item, or throw ResourceNotFoundException if it doesn't exist.
     *
     * @param id - ID of item to be deleted
     * @throws ResourceNotFoundException if missing
     */
    public void deleteById(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id " + id);
        }
        itemRepository.deleteById(id);
    }

    /**
     * Update fields of an existing item.
     *
     * @param id   - ID of item to update
     * @param item - data to apply
     * @return updated Item
     * @throws ResourceNotFoundException if missing
     */
    public Item updateItem(Long id, Item item) {
        Item existing = findByIdOrThrow(id);
        existing.setName(item.getName());
        existing.setDescription(item.getDescription());
        existing.setStatus(item.getStatus());
        existing.setEmail(item.getEmail());
        return itemRepository.save(existing);
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * Asynchronously process all items:
     * - fetch each,
     * - update its status,
     * - save it,
     * - collect into a list.
     *
     * @return a CompletableFuture that completes when all items are done.
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        // Task 7i: fetch all IDs ahead of time
        List<Long> itemIds = itemRepository.findAllIds();

        // Task 7i/7ii: for each ID, submit a supplier that processes it and
        // returns the Item or null on failure
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id ->
                        CompletableFuture.supplyAsync(() -> {
                                    try {
                                        // simulate work
                                        Thread.sleep(100);

                                        // retrieve
                                        Item item = itemRepository.findById(id).orElse(null);
                                        if (item == null) {
                                            return null;
                                        }

                                        if ("PROCESSED".equalsIgnoreCase(item.getStatus())) {
                                            return null;
                                        }

                                        // update and save
                                        item.setStatus("PROCESSED");
                                        return itemRepository.save(item);

                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return null;
                                    }
                                }, executor)
                                // Task 7iii: if any one fails, return null and swallow or log exception
                                .exceptionally(ex -> {
                                    // log here ex.getMessage()
                                    return null;
                                })
                )
                .toList();

        // Task 7iii: combine all into one future
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));

        // Task 7iii/iv: when all are done, build the list of non-null results
        CompletableFuture<List<Item>> resultList = allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join) //safe: all have completed
                        .filter(Objects::nonNull) // Task 7ii: only those processed successfully
                        .toList()
        );
        return resultList;
    }
}