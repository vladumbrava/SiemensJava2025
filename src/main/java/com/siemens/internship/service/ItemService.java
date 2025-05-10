package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Issues:
 * - field injection is not recommended in spring boot
 * - ExecutorService is less suitable in the Spring context (improper shutdown management,
 *   resource leaks, unpredictable behaviour combined with @Async)
 * - a method annotated with @Async must have a void or Future-like type
 * - errors are simply printed, but not properly managed
 * - the method returns before all items are processed
 * - it is a bad practice to have processedItems as a field of the service, because it is
 *   initialized in the initialization moment of the service and would need to be reinitialized
 *   at each processItemsAsync() method call, which does not make sense
 * - similarly, processedCount is defined as a field of the service, although it should be
 *   a variable within the asynchronous method
 * Solutions:
 * - approach constructor injection to promote immutability and testability
 * - use TaskExecutor, it is designed for Spring's asynchronous execution (managed as a
 *   Spring bean, allowing proper lifecycle management)
 * - modify the return type of the @Async annotated method to Future-like type
 * - delete the fields processedItems and processedCount
 */

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final TaskExecutor executor;

    @Autowired
    public ItemService(ItemRepository itemRepository, @Qualifier("taskExecutor") TaskExecutor executor) {
        this.itemRepository = itemRepository;
        this.executor = executor;
    }

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

        List<Long> itemIds = itemRepository.findAllIds();
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Item item = itemRepository.findById(id).orElseThrow();
                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    return item;
                } catch (Exception e) {
                    System.err.println("Error processing item with ID: " + id);
                    return null;
                }
            }, executor);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }

}

