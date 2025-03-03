package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUserUserID(Long userID);

	// ✅ Fetch all city names from `city` table
	@Query(value = "SELECT city_name FROM city", nativeQuery = true)
	List<String> findAllCityNames();

	// ✅ Fetch township names based on `city_name`
	@Query(value = "SELECT township_name FROM township WHERE city_id = (SELECT city_id FROM city WHERE city_name = :cityName)", nativeQuery = true)
	List<String> findTownshipNamesByCity(@Param("cityName") String cityName);

	List<Address> findByUser(User user);
}
