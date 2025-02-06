package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "category_tbl")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long catID;

	@Column(length = 50)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parentID")
	@JsonIgnore // Prevent infinite recursion
	private Category parentCategory;

	@Transient // This is not stored in the DB but will be included in the JSON response
	private List<Category> subcategories;

	// Getters and Setters
	public Long getCatID() {
		return catID;
	}

	public void setCatID(Long catID) {
		this.catID = catID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	public String getFullCategoryPath() {
		if (parentCategory != null) {
			return parentCategory.getFullCategoryPath() + " > " + name;
		}
		return name;
	}

	public List<Category> getSubcategories() {
		return subcategories;
	}

	public void setSubcategories(List<Category> subcategories) {
		this.subcategories = subcategories;
	}

	public List<Category> getCategoryPathList() {
		List<Category> pathList = new ArrayList<>();
		Category current = this;

		while (current != null) {
			pathList.add(0, current); // Add to the beginning to maintain correct order
			current = current.getParentCategory();
		}

		return pathList;
	}
}
