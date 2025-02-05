package com.example.demo.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Category;


public interface CategoryRepository extends JpaRepository<Category, Long>{
	

	 // Fetch subcategories based on parent category
   List<Category> findByParentCategory(Category parentCategory);

   // Fetch top-level categories (where parentCategory is NULL)
   List<Category> findByParentCategoryIsNull();

}