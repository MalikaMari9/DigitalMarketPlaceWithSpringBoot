package com.example.demo.repository.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Item;
import com.example.demo.entity.tag.ItemTag;
import com.example.demo.entity.tag.Tag;

public interface ItemTagRepository extends JpaRepository<ItemTag, Long> {
	boolean existsByItemAndTag(Item item, Tag tag);

}
