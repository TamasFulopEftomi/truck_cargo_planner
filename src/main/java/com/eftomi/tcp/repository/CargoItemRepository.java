package com.eftomi.tcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eftomi.tcp.entity.CargoItem;

@Repository
public interface CargoItemRepository extends JpaRepository<CargoItem, Integer> {
	List<CargoItem> findAll();
}
