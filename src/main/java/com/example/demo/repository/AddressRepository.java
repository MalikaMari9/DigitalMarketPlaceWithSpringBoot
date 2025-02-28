package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUserUserID(Long userID);
}
