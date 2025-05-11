package com.siemens.internship;

import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemControllerTests {

    @Mock
    private ItemService itemService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllItems_ShouldReturnOK() {
        when(itemService.findAll()).thenReturn(List.of(new Item()));

        ResponseEntity<List<Item>> response = itemController.getAllItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createItem_ShouldReturnCreated_WhenValid() {
        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setStatus("NEW");
        item.setEmail("test@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(itemService.save(item)).thenReturn(item);

        ResponseEntity<Item> response = itemController.createItem(item, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(item, response.getBody());
        verify(itemService, times(1)).save(item);
    }

    @Test
    void createItem_ShouldReturnBadRequest_WhenInvalid() {
        Item item = new Item();
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<Item> response = itemController.createItem(item, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(itemService, never()).save(any());
    }

    @Test
    void getItemById_ShouldReturnOK_WhenExists() {
        Item item = new Item();
        item.setId(1L);
        item.setName("test name");
        item.setDescription("test description");
        item.setStatus("NEW");
        item.setEmail("test@example.com");

        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(item, response.getBody());
    }

    @Test
    void getItemById_ShouldReturnNotFound_WhenNotExists() {
        when(itemService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.getItemById(5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateItem_ShouldReturnOK_WhenIsPresent() {
        Item item = new Item();
        item.setName("test name");
        item.setDescription("test description");
        item.setStatus("NEW");
        item.setEmail("test@example.com");

        when(itemService.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemService.save(any())).thenReturn(item);

        ResponseEntity<Item> response = itemController.updateItem(2L, item);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(item, response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void updateItem_ShouldReturnNotFound_WhenIsNotPresent() {
        when(itemService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.updateItem(1L, new Item());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteItem_ShouldReturnNoContent_WhenNoError() {
        Long id = 1L;

        doNothing().when(itemService).deleteById(id);

        ResponseEntity<Void> response = itemController.deleteItem(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(itemService, times(1)).deleteById(id);
    }

    @Test
    void deleteItem_ShouldReturnNotFound_WhenNoSuchElementException() {
        Long id = 1L;

        doThrow(new NoSuchElementException()).when(itemService).deleteById(id);

        ResponseEntity<Void> response = itemController.deleteItem(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(itemService, times(1)).deleteById(id);
    }

    @Test
    void deleteItem_ShouldReturnServerError_WhenException() {
        Long id = 1L;

        doThrow(new RuntimeException()).when(itemService).deleteById(id);

        ResponseEntity<Void> response = itemController.deleteItem(id);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(itemService, times(1)).deleteById(id);
    }

    @Test
    void processItems_ShouldReturnOK_WhenItemsProcessed() {
        List<Item> processedItems = List.of(new Item());
        CompletableFuture<List<Item>> future = CompletableFuture.completedFuture(processedItems);

        when(itemService.processItemsAsync()).thenReturn(future);

        ResponseEntity<List<Item>> response = itemController.processItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void processItems_ShouldReturnNoContent_WhenNoItemsProcessed() {
        CompletableFuture<List<Item>> future = CompletableFuture.completedFuture(List.of());

        when(itemService.processItemsAsync()).thenReturn(future);

        ResponseEntity<List<Item>> response = itemController.processItems();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void processItems_ShouldReturnServerError_WhenExceptionOccurs() {
        when(itemService.processItemsAsync()).thenThrow(new RuntimeException("Processing error"));

        ResponseEntity<List<Item>> response = itemController.processItems();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

}
