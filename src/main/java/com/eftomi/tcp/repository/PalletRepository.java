package com.eftomi.tcp.repository;

import com.eftomi.tcp.entity.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, Integer> {
	List<Pallet> findAll();
}
