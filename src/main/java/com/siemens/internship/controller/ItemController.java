package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Issues:
 * - field injection is not recommended (for itemService)
 * - in createItem(), the status codes for success and error are reversed
 * - similarly, in updateItem(), it returns accepted when the item is not found,
 *   and created when found
 * - in getItemById(), NOT_FOUND is more suitable than NO_CONTENT
 * - CONFLICT in deleteItem() does not indicate a successful deletion
 * - processItems() always returns OK
 * Solutions:
 * - apply constructor injection instead
 * - replace the status codes
 * - add a check for an empty result in processItems()
 */

@RestController
@RequestMapping("/api/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.findAll();
        log.info("Found {} items.", items.size());
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            log.error("Validation failed for item: {}", item);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Item savedItem = itemService.save(item);
        log.info("Item created successfully with ID: {}", savedItem.getId());
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            Item updatedItem = itemService.save(item);
            log.info("Item with ID: {} updated successfully.", id);
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteById(id);
            log.info("Item with ID: {} deleted successfully.", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            log.warn("Item with ID: {} not found for deletion.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("An error occurred while deleting item with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        log.info("Processing items asynchronously.");
        try {
            CompletableFuture<List<Item>> processedItemsFuture = itemService.processItemsAsync();
            List<Item> processedItems = processedItemsFuture.join();

            if (processedItems.isEmpty()) {
                log.warn("No items were processed.");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(processedItems, HttpStatus.OK);
        } catch (Exception e) {
            log.error("An error occurred while processing items.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
