package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
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
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        // Step 1: Fetch all item IDs from the database.
        // This is more efficient than fetching full entities if we only need IDs at first.
        List<Long> itemIds = itemRepository.findAllIds();

        // Step 2: Use a thread-safe list to store processed items.
        // This avoids concurrency issues when multiple threads try to add items simultaneously.
        List<Item> processedItems = new CopyOnWriteArrayList<>();

        // Step 3: For each item ID, create an asynchronous task that:
        // - Fetches the item
        // - Sets its status to "PROCESSED"
        // - Saves the updated item to the database
        // - Adds it to the result list
        // All tasks are collected into a list of CompletableFutures.
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        // Try to find the item by ID. If not found, throw an error immediately.
                        Item item = itemRepository.findById(id).orElseThrow(() ->
                                new RuntimeException("Item not found"));

                        // Update the item's status.
                        item.setStatus("PROCESSED");

                        // Persist the updated item in the database.
                        itemRepository.save(item);

                        // Add the item to the shared result list.
                        processedItems.add(item);

                    } catch (Exception e) {
                        // If any exception occurs during processing, wrap it in a runtime exception.
                        throw new RuntimeException("Error processing item", e);
                    }
                }, executor)) // Run each task using the custom executor for better thread control.
                .collect(Collectors.toUnmodifiableList());

        // Step 4: Combine all CompletableFutures into a single one using allOf().
        // When all are complete, return the list of successfully processed items.
        // If any task failed, handle the exception and propagate it properly.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems) // Return the collected list once all tasks finish.
                .exceptionally(exception -> {
                    // Handle any exceptions that occurred in any of the tasks.
                    // This wraps the original exception for proper propagation up the call stack.
                    throw new CompletionException("Error while processing items asynchronously", exception);
                });
    }

}

