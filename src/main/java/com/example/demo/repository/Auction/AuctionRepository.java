package com.example.demo.repository.Auction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Auction.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long>{
	
	Auction findByItem_ItemID(Long itemID);
}