package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ItemServiceTests {

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        List<Item> items = List.of(new Item(), new Item());
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertEquals(2, result.size());
        assertEquals(items, result);
    }

    @Test
    void findById_ShouldReturnItem_WhenItemExists() {
        Long id = 1L;
        Item item = new Item();
        item.setId(id);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Optional<Item> result = itemService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(item, result.get());
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenItemDoesNotExist() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Item> result = itemService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldReturnSavedItem() {
        Item item = new Item();
        item.setName("Test Item");

        when(itemRepository.save(item)).thenReturn(item);

        Item result = itemService.save(item);

        assertEquals(item, result);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void save_ShouldThrowException_WhenRepositoryFails() {
        Item item = new Item();
        item.setName("Test Item");

        when(itemRepository.save(item)).thenThrow(new RuntimeException("Save failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> itemService.save(item));

        assertEquals("Save failed", exception.getMessage());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void deleteById_ShouldDeleteItem_WhenItemExists() {
        Long id = 1L;

        when(itemRepository.existsById(id)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(id);

        assertDoesNotThrow(() -> itemService.deleteById(id));
        verify(itemRepository, times(1)).existsById(id);
        verify(itemRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_ShouldThrowNoSuchElementException_WhenItemDoesNotExist() {
        Long id = 1L;

        when(itemRepository.existsById(id)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> itemService.deleteById(id));
        assertEquals("Item with ID " + id + " not found", exception.getMessage());
        verify(itemRepository, times(1)).existsById(id);
        verify(itemRepository, never()).deleteById(id);
    }

}
