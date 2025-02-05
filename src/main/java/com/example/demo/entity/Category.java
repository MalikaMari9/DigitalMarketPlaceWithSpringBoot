package com.example.demo.entity;

import jakarta.persistence.*;

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
	private Category parentCategory;

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
}
