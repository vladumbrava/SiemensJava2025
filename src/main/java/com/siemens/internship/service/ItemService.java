package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Issues:
 * - field injection is not recommended in spring boot
 * - ExecutorService is less suitable in the Spring context (improper shutdown management,
 *   resource leaks, unpredictable behaviour combined with @Async)
 * Solutions:
 * - approach constructor injection to promote immutability and testability
 * - use TaskExecutor, it is designed for Spring's asynchronous execution (managed as a
 *   Spring bean, allowing proper lifecycle management)
 */

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final TaskExecutor executor;
    // use thread-safe collection to prevent potential concurrent
    // modification issues when multiple threads try to add items simultaneously
    private final List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());
    private int processedCount = 0;

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
    public List<Item> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    processedCount++;

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    processedItems.add(item);

                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }, executor);
        }

        return processedItems;
    }

}

