package com.example.demo.entity.Auction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.demo.entity.User;

@Entity
@Table(name = "auctiontrack_tbl")
public class AuctionTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionTrackID;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "auctionID", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    // Getters and Setters
    public Long getAuctionTrackID() {
        return auctionTrackID;
    }

    public void setAuctionTrackID(Long auctionTrackID) {
        this.auctionTrackID = auctionTrackID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
