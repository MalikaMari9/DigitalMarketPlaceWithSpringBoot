package com.example.demo.entity.tag;

import com.example.demo.entity.Item;

import jakarta.persistence.*;

@Entity
@Table(name = "itemtag_tbl")
public class ItemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemTagID")
    private Long itemTagID;

    @ManyToOne
    @JoinColumn(name = "tag_tbl_tagID", nullable = false)
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "item_tbl_itemID", nullable = false)
    private Item item;

    // Getters and Setters
    public Long getItemTagID() {
        return itemTagID;
    }

    public void setItemTagID(Long itemTagID) {
        this.itemTagID = itemTagID;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
