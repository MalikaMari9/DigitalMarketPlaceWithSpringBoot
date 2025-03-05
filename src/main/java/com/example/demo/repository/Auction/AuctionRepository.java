package com.example.demo.repository.Auction;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;
import com.example.demo.entity.Auction.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

	Auction findByItem_ItemID(Long itemID);

	@Query("SELECT a FROM Auction a WHERE a.item IN :items")
	List<Auction> findAllByItemIn(@Param("items") List<Item> items);

	List<Auction> findByEndTimeBeforeAndStat(LocalDateTime now, Auction.AuctionStatus status);

	Auction findByItem(Item item);
}