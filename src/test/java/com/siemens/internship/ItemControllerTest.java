package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import com.siemens.internship.utils.CustomExceptionHandler;
import com.siemens.internship.utils.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(CustomExceptionHandler.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test that GET /api/items should return OK (200) with a JSON path array
     */
    @Test
    void testGetAllItems() throws Exception {
        when(itemService.findAll())
                .thenReturn(List.of(new Item(1L, "Name", "Desc", "UNPROCESSED", "test@example.com")));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    /**
     * Test that when we POST a valid Item, it returns CREATED (201) with JSON body
     */
    @Test
    void testCreateItemValid() throws Exception {
        Item inputItem = new Item(null, "NamePOST", "Desc", "STATUS", "test@example.com");
        Item savedItem = new Item(2L, "NamePOST", "Desc", "STATUS", "test@example.com");
        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    /**
     * Test that when we POST an invalid Item (blank name, bad email), we get BAD_REQUEST (400)
     */
    @Test
    void testCreateItemInvalid() throws Exception {
        Item invalidItem = new Item(null, "", "Desc", "STATUS", "not-an-email");
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that GET /api/items/{id} returns OK (200) when the item has been found
     */
    @Test
    void testGetItemByIdFound() throws Exception {
        when(itemService.findByIdOrThrow(1L))
                .thenReturn(new Item(1L, "NameGet", "Desc", "UNPROCESSED", "test@example.com"));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NameGet"));
    }

    /**
     * Test that GET /api/items/{id} returns NOT_FOUND (404) when not found
     */
    @Test
    void testGetItemByIdNotFound() throws Exception {
        when(itemService.findByIdOrThrow(1L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that the PUT operation with a valid Item returns OK (200)
     */
    @Test
    void testUpdateItemValid() throws Exception {
        Item in = new Item(null,"NamePUT","Description","STATUS","test@example.com");
        Item out = new Item(1L,"NamePUT","Description","STATUS","test@example.com");
        when(itemService.updateItem(eq(1L), any(Item.class))).thenReturn(out);

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Test that the PUT operation with an invalid Item returns BAD_REQUEST (400)
     */
    @Test
    void testUpdateItemInvalid() throws Exception {
        Item bad = new Item(null,"","Description","STATUS","not-an-email");
        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that DELETE works and returns NO_CONTENT (204)
     */
    @Test
    void testDeleteItem() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }
}
