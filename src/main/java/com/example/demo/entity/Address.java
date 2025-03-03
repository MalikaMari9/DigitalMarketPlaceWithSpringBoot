package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "address_tbl")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long addressID;

	@Column(length = 40)
	private String custName;

	@Column(length = 40)
	private String building;

	@Column(columnDefinition = "TEXT")
	private String addres; // ✅ Stores detailed address

	@Column(length = 40)
	private String city;

	@Column(length = 40)
	private String postalCode;

	@Column(length = 40)
	private String phone;

	@Column(length = 40)
	private String township;

	@Column(length = 50) // ✅ New column for address name
	private String addressName;

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	@Column(columnDefinition = "TEXT")
	private String delinote; // ✅ Extra delivery note

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false) // ✅ Links to User
	private User user;

	// ✅ Getters and Setters
	public Long getAddressID() {
		return addressID;
	}

	public void setAddressID(Long addressID) {
		this.addressID = addressID;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getAddres() {
		return addres;
	}

	public void setAddres(String addres) {
		this.addres = addres;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTownship() {
		return township;
	}

	public void setTownship(String township) {
		this.township = township;
	}

	public String getDelinote() {
		return delinote;
	}

	public void setDelinote(String delinote) {
		this.delinote = delinote;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
