package com.eftomi.tcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eftomi.tcp.entity.Box;

@Repository
public interface BoxRepository extends JpaRepository<Box, Integer> {
	List<Box> findAll();
}
