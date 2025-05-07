package com.siemens.internship.service;

import com.siemens.internship.exception.ResourceAlreadyExistsException;
import com.siemens.internship.exception.ResourceNotFoundException;
import com.siemens.internship.mapper.ItemMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.request.ItemRequest;
import com.siemens.internship.response.ItemResponse;
import com.siemens.internship.service.implementation.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTests {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        item = Item.builder()
                .id(1L)
                .name("TestItem")
                .description("Description")
                .status("NEW")
                .email("test@example.com")
                .build();

        itemRequest = new ItemRequest("TestItem", "Description", "NEW", "test@example.com");
        itemResponse = new ItemResponse(1L, "TestItem", "Description", "NEW", "test@example.com");
    }

    @Test
    void testFindById_found() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemService.findById(1L);

        assertThat(response).isEqualTo(itemResponse);
    }

    @Test
    void testFindById_notFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testFindAll_found() {
        Page<Item> items = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(items);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        Page<ItemResponse> result = itemService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).containsExactly(itemResponse);
    }

    @Test
    void testFindAll_notFound() {
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThatThrownBy(() -> itemService.findAll(Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testSave_success() {
        when(itemRepository.findByNameIgnoreCase("TestItem")).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemService.save(itemRequest);

        assertThat(response).isEqualTo(itemResponse);
    }

    @Test
    void testSave_alreadyExists() {
        when(itemRepository.findByNameIgnoreCase("TestItem")).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.save(itemRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void testUpdateById_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemService.updateById(1L, itemRequest);

        assertThat(response).isEqualTo(itemResponse);
    }

    @Test
    void testUpdateById_notFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateById(1L, itemRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testDeleteById_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(item);

        itemService.deleteById(1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void testDeleteById_notFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testProcessItemsAsync_success() throws ExecutionException, InterruptedException {
        Item processedItem = Item.builder().id(1L).name("TestItem").description("Desc").status("PROCESSED").email("test@example.com").build();

        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(processedItem);
        when(itemMapper.toItemResponse(processedItem)).thenReturn(itemResponse);

        CompletableFuture<List<ItemResponse>> future = itemService.processItemsAsync();
        List<ItemResponse> result = future.get();

        assertThat(result).containsExactly(itemResponse);
    }

    @Test
    void testProcessItemsAsync_emptyList() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> itemService.processItemsAsync().join())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}