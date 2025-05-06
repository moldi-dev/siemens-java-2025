package com.siemens.internship.service;

import com.siemens.internship.request.ItemRequest;
import com.siemens.internship.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface IItemService {
    Page<ItemResponse> findAll(Pageable pageable);
    ItemResponse findById(Long id);
    ItemResponse save(ItemRequest itemRequest);
    ItemResponse updateById(Long id, ItemRequest itemRequest);
    void deleteById(Long id);
    CompletableFuture<List<ItemResponse>> processItemsAsync();
}
