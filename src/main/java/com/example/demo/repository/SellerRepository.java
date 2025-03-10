package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Seller;
import com.example.demo.entity.User;

public interface SellerRepository extends JpaRepository<Seller, Long> {

	@Query(value = "SELECT name_en FROM nrcs WHERE nrc_code = :cityCode", nativeQuery = true)
	List<String> findNamesByCityCode(@Param("cityCode") Integer cityCode);

	Seller findByUser(User user);

	Page<Seller> findByApproval(String approval, Pageable pageable);

	List<Seller> findByApproval(String string);

	int countByApproval(String string);
}
