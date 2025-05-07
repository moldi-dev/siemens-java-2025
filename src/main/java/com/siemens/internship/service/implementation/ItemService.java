package com.siemens.internship.service.implementation;

import com.siemens.internship.exception.ResourceAlreadyExistsException;
import com.siemens.internship.exception.ResourceNotFoundException;
import com.siemens.internship.mapper.ItemMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.request.ItemRequest;
import com.siemens.internship.response.ItemResponse;
import com.siemens.internship.service.IItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService implements IItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public Page<ItemResponse> findAll(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);

        if (items.isEmpty()) {
            log.error("[ItemService] No items found, throwing a not found exception");
            throw new ResourceNotFoundException("No items could be found");
        }

        return items.map(itemMapper::toItemResponse);
    }

    @Override
    public ItemResponse findById(Long id) {
        return itemRepository
                .findById(id)
                .map(itemMapper::toItemResponse)
                .orElseGet(() -> {
                    log.error("[ItemService] The item by the provided id \"{}\" couldn't be found, throwing a not found exception", id);
                    throw new ResourceNotFoundException(String.format("The item by the provided id (%d) couldn't be found", id));
                });
    }

    @Override
    public ItemResponse save(ItemRequest itemRequest) {
        // I'm not going to allow 2 items to have the exact same name, wouldn't make sense
        // (also not specified in the requirements)

        // Also, why do items have emails?

        Optional<Item> searchedItemByName = itemRepository
                .findByNameIgnoreCase(itemRequest.name());

        if (searchedItemByName.isPresent()) {
            log.error("[ItemService] An item with this name \"{}\" already exists, throwing a conflict exception", itemRequest.name());
            throw new ResourceAlreadyExistsException(String.format("An item with this name (%s) already exists", itemRequest.name()));
        }

        Item itemToSave = Item
                .builder()
                .name(itemRequest.name())
                .description(itemRequest.description())
                .status(itemRequest.status())
                .email(itemRequest.email())
                .build();

        return itemMapper.toItemResponse(itemRepository.save(itemToSave));
    }

    @Override
    public ItemResponse updateById(Long id, ItemRequest itemRequest) {
        Item itemToUpdate = itemRepository
                .findById(id)
                .orElseGet(() -> {
                    log.error("[ItemService] The item by the provided id \"{}\" couldn't be found, throwing a not found exception", id);
                    throw new ResourceNotFoundException(String.format("The item by the provided id (%d) couldn't be found", id));
                });

        itemToUpdate.setName(itemRequest.name());
        itemToUpdate.setDescription(itemRequest.description());
        itemToUpdate.setStatus(itemRequest.status());
        itemToUpdate.setEmail(itemRequest.email());

        return itemMapper.toItemResponse(itemRepository.save(itemToUpdate));
    }

    @Override
    public void deleteById(Long id) {
        Item itemToDelete = itemRepository
                .findById(id)
                .orElseGet(() -> {
                    log.error("[ItemService] The item by the provided id \"{}\" couldn't be found, throwing a not found exception", id);
                    throw new ResourceNotFoundException(String.format("The item by the provided id (%d) couldn't be found", id));
                });

        itemRepository.delete(itemToDelete);
    }

    @Async
    @Override
    public CompletableFuture<List<ItemResponse>> processItemsAsync() {
        // Fetch all the items from the database and check if we have any to process
        List<Item> items = itemRepository.findAll();

        if (items.isEmpty()) {
            log.error("[ItemService] No items found, throwing a not found exception");
            throw new ResourceNotFoundException("No items could be found");
        }

        // Process each item asynchronously
        List<CompletableFuture<ItemResponse>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Simulate a processing delay
                        TimeUnit.MILLISECONDS.sleep(100);

                        // Update the current item's status
                        item.setStatus("PROCESSED");

                        // Save the updated item
                        Item savedItem = itemRepository.save(item);

                        // Map to response
                        return itemMapper.toItemResponse(savedItem);
                    }

                    catch (InterruptedException e) {
                        log.error("[ItemService] Interrupted while waiting for processing of items: {}", e.getMessage());
                        Thread.currentThread().interrupt(); // Preserve thread interrupt status
                        return null; // Skip this item
                    }

                    catch (Exception e) {
                        // Log and skip the failed item
                        log.error("[ItemService] Failed to process the item with id \"{}\" | {}", item.getId(), e.getMessage());
                        return null;
                    }
                }))
                .toList();

        // Wait for all processing to complete and return only the successfully processed items
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
    }
}

