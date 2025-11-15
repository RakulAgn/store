package com.buggybot.store.controller.store.interfaces;

import com.buggybot.store.controller.store.dto.StoreDTO;
import com.buggybot.store.controller.store.responseEntity.Store;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface StoreService {
    List<Store> getAllStores();
    Optional<Store> getStoreById(UUID id);
    Store createStore(StoreDTO dto);
    Optional<Store> replaceStore(UUID id, StoreDTO dto);
    Optional<Store> patchStore(UUID id, Map<String, Object> updates);
    boolean deleteStore(UUID id);
}