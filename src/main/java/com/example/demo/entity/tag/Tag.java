package com.example.demo.entity.tag;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "tag_tbl")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagID")
    private Long tagID;

    @Column(name = "name", length = 100)
    private String name;
    
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<ItemTag> itemTags; // ✅ Relation with itemtag_tbl


    // Getters and Setters
    public Long getTagID() {
        return tagID;
    }

    public void setTagID(Long tagID) {
        this.tagID = tagID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

