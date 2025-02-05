package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ItemImage;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long>{
	
	
}

