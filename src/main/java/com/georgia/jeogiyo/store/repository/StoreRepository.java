package com.georgia.jeogiyo.store.repository;

import com.georgia.jeogiyo.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

}
