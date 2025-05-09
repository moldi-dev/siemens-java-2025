package com.siemens.internship.controller;

import com.siemens.internship.request.ItemRequest;
import com.siemens.internship.response.HttpResponse;
import com.siemens.internship.response.ItemResponse;
import com.siemens.internship.service.implementation.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/items") // Better to include API versioning
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<HttpResponse> getAllItems(Pageable pageable) {
        log.info("[ItemController] Getting all items with the following pagination parameters: {}", pageable);

        Page<ItemResponse> result = itemService.findAll(pageable);

        return ResponseEntity.ok(
                HttpResponse
                        .builder()
                        .timestamp(LocalDateTime.now().toString())
                        .responseMessage("The items have been found successfully")
                        .responseStatus(HttpStatus.OK)
                        .responseStatusCode(HttpStatus.OK.value())
                        .body(result)
                        .build()
        );
    }

    @GetMapping("/id={id}")
    public ResponseEntity<HttpResponse> getItemById(@PathVariable("id") Long id) {
        log.info("[ItemController] Getting an item by id \"{}\"", id);

        ItemResponse result = itemService.findById(id);

        return ResponseEntity.ok(
                HttpResponse
                        .builder()
                        .timestamp(LocalDateTime.now().toString())
                        .responseMessage("The item has been found successfully")
                        .responseStatus(HttpStatus.OK)
                        .responseStatusCode(HttpStatus.OK.value())
                        .body(result)
                        .build()
        );
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<HttpResponse>> processItemsAsync() {
        log.info("[ItemController] Processing all items");

        return itemService.processItemsAsync()
                .thenApply(processedItems ->
                        ResponseEntity.accepted().body(
                                HttpResponse.builder()
                                        .timestamp(LocalDateTime.now().toString())
                                        .responseMessage("The items were processed successfully")
                                        .responseStatus(HttpStatus.ACCEPTED)
                                        .responseStatusCode(HttpStatus.ACCEPTED.value())
                                        .body(processedItems)
                                        .build()
                        ));
    }

    @PostMapping
    public ResponseEntity<HttpResponse> createItem(@Valid @RequestBody ItemRequest request) {
        log.info("[ItemController] Saving an item with the following request body: {}", request);

        ItemResponse result = itemService.save(request);

        URI location = URI.create("/api/v1/items/" + result.id());

        return ResponseEntity.created(location).body(
                HttpResponse
                        .builder()
                        .timestamp(LocalDateTime.now().toString())
                        .responseMessage("The item has been saved successfully")
                        .responseStatus(HttpStatus.CREATED)
                        .responseStatusCode(HttpStatus.CREATED.value())
                        .body(result)
                        .build()
        );
    }

    @PutMapping("/id={id}")
    public ResponseEntity<HttpResponse> updateItem(@PathVariable("id") Long id, @Valid @RequestBody ItemRequest request) {
        log.info("[ItemController] Updating an item with the following id and request body: \"{}\" | {}", id, request);

        ItemResponse result = itemService.updateById(id, request);

        return ResponseEntity.ok(
                HttpResponse
                        .builder()
                        .timestamp(LocalDateTime.now().toString())
                        .responseMessage("The item has been updated successfully")
                        .responseStatus(HttpStatus.OK)
                        .responseStatusCode(HttpStatus.OK.value())
                        .body(result)
                        .build()
        );
    }

    @DeleteMapping("/id={id}")
    public ResponseEntity<HttpResponse> deleteItem(@PathVariable("id") Long id) {
        log.info("[ItemController] Deleting an item with the following id: \"{}\"", id);

        itemService.deleteById(id);

        return ResponseEntity.ok(
                HttpResponse
                        .builder()
                        .timestamp(LocalDateTime.now().toString())
                        .responseMessage("The item has been deleted successfully")
                        .responseStatus(HttpStatus.OK)
                        .responseStatusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}
