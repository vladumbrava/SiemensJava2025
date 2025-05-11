package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.TaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
class ItemServiceAsyncTests {

    @MockBean
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    @Autowired
    @Qualifier("taskExecutor")
    TaskExecutor executor;


    @Test
    void processItemsAsync_ShouldProcessAllItemsSuccessfully() throws Exception {
        List<Long> itemIds = Arrays.asList(1L, 2L);
        Item item1 = new Item();
        item1.setId(1L);
        item1.setStatus("NEW");
        Item item2 = new Item();
        item2.setId(2L);
        item2.setStatus("NEW");

        when(itemRepository.findAllIds()).thenReturn(itemIds);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        assertEquals(2, processedItems.size());
        assertEquals("PROCESSED", processedItems.get(0).getStatus());
        assertEquals("PROCESSED", processedItems.get(1).getStatus());
        verify(itemRepository, times(2)).save(any(Item.class));
    }

    @Test
    void processItemsAsync_ShouldHandleEmptyItemList() throws Exception {
        when(itemRepository.findAllIds()).thenReturn(Collections.emptyList());

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        assertTrue(processedItems.isEmpty());
        verify(itemRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void processItemsAsync_ShouldSkipItemsWithErrors() throws Exception {
        List<Long> itemIds = Arrays.asList(1L, 2L);
        Item item1 = new Item();
        item1.setId(1L);
        item1.setStatus("NEW");

        when(itemRepository.findAllIds()).thenReturn(itemIds);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenThrow(new RuntimeException("Error fetching item"));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        assertEquals(1, processedItems.size());
        assertEquals("PROCESSED", processedItems.get(0).getStatus());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void processItemsAsync_ShouldHandleSaveErrorsGracefully() throws Exception {
        List<Long> itemIds = Arrays.asList(1L, 2L);
        Item item1 = new Item();
        item1.setId(1L);
        item1.setStatus("NEW");
        Item item2 = new Item();
        item2.setId(2L);
        item2.setStatus("NEW");

        when(itemRepository.findAllIds()).thenReturn(itemIds);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(item1)).thenThrow(new RuntimeException("Error saving item"));
        when(itemRepository.save(item2)).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        assertEquals(1, processedItems.size());
        assertEquals("PROCESSED", processedItems.get(0).getStatus());
        verify(itemRepository, times(2)).save(any(Item.class));
    }
}
