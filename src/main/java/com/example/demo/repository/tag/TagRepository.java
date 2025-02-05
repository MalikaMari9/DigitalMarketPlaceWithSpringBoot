package com.example.demo.repository.tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.tag.Tag;

public interface TagRepository extends JpaRepository<Tag, Long>{
	
	Optional<Tag> findByName(String name);
}
