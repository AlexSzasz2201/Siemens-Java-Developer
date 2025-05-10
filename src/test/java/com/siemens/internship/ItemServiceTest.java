package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import com.siemens.internship.utils.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        item = new Item(1L, "Item1", "Desc", "NEW", "test@example.com");
    }

    /**
     * We test if findAll() returns what the repository provides
     */
    @Test
    void testFindAll() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        List<Item> items = itemService.findAll();
        assertEquals(1, items.size());
        assertSame(item, items.get(0));
        verify(itemRepository).findAll();
    }

    /**
     * We test if findByIdOrThrow() returns an item when present
     */
    @Test
    void testFindByIdOrThrow() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Item result = itemService.findByIdOrThrow(1L);
        assertSame(item, result);
    }

    /**
     * We test if findByIdOrThrow() throws ResourceNotFoundException when missing
     */
    @Test
    void testFindByIdOrThrowNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemService.findByIdOrThrow(2L));
    }

    /**
     * We test if save() delegates to repository.save()
     */
    @Test
    void testSave() {
        when(itemRepository.save(item)).thenReturn(item);
        Item result = itemService.save(item);
        assertSame(item, result);
        verify(itemRepository).save(item);
    }

    /**
     * Test that updateItem() applies fields and saves
     */
    @Test
    void testUpdate() {
        Item updatedInfo = new Item(null, "asdasd", "qweqwe", "PROCESSED", "updated@example.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.updateItem(1L, updatedInfo);
        assertEquals("asdasd", result.getName());
        assertEquals("qweqwe", result.getDescription());
        assertEquals("PROCESSED", result.getStatus());
        assertEquals("updated@example.com", result.getEmail());
    }

    /**
     * Test that deleteById() throws ResourceNotFoundException when item does not exist
     */
    @Test
    void testDeleteByIdNotFound() {
        when(itemRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteById(1L));
    }

    /**
     * Task 7: processItemsAsync should process all IDs and return only the successfully processed items.
     */
    @Test
    void testProcessItemsAsync() throws Exception {
        // we have two IDs, only the first resolves to an existing Item
        List<Long> ids = List.of(1L, 2L);
        when(itemRepository.findAllIds()).thenReturn(ids);

        Item item1 = new Item(1L, "Name1L", "Desc1L", "NEW", "onel@test.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        // echo back the saved entity
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        // we trigger async processing
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.get(2, TimeUnit.SECONDS);

        // only one item was successfully processed
        assertEquals(1, processed.size());
        Item processedItem = processed.get(0);
        assertEquals(1L, processedItem.getId());
        assertEquals("PROCESSED", processedItem.getStatus());

        // verify that save(...) was indeed called once
        verify(itemRepository, times(1)).save(any(Item.class));
    }
}
