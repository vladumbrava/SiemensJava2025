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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
