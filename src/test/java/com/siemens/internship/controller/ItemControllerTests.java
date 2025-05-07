package com.siemens.internship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.request.ItemRequest;
import com.siemens.internship.response.ItemResponse;
import com.siemens.internship.service.implementation.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemResponse itemResponse;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        itemResponse = new ItemResponse(1L, "TestItem", "Test description", "NEW", "test@example.com");
        itemRequest = new ItemRequest("TestItem", "Test description", "NEW", "test@example.com");
    }

    @Test
    void testGetAllItems() throws Exception {
        Page<ItemResponse> page = new PageImpl<>(List.of(itemResponse));
        when(itemService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage").value("The items have been found successfully"))
                .andExpect(jsonPath("$.body.content[0].name").value("TestItem"));
    }

    @Test
    void testGetItemById() throws Exception {
        when(itemService.findById(1L)).thenReturn(itemResponse);

        mockMvc.perform(get("/api/v1/items/id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage").value("The item has been found successfully"))
                .andExpect(jsonPath("$.body.name").value("TestItem"));
    }

    @Test
    void testCreateItem() throws Exception {
        when(itemService.save(any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/items/1"))
                .andExpect(jsonPath("$.responseMessage").value("The item has been saved successfully"))
                .andExpect(jsonPath("$.body.name").value("TestItem"));
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.updateById(Mockito.eq(1L), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(put("/api/v1/items/id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage").value("The item has been updated successfully"))
                .andExpect(jsonPath("$.body.name").value("TestItem"));
    }

    @Test
    void testDeleteItem() throws Exception {
        mockMvc.perform(delete("/api/v1/items/id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseMessage").value("The item has been deleted successfully"));
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        ItemResponse itemResponse = new ItemResponse(1L, "TestItem", "Test description", "PROCESSED", "test@example.com");

        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(List.of(itemResponse)));

        var mvcResult = mockMvc.perform(get("/api/v1/items/process"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.responseMessage").value("The items were processed successfully"))
                .andExpect(jsonPath("$.body[0].name").value("TestItem"))
                .andExpect(jsonPath("$.body[0].status").value("PROCESSED"));
    }
}
