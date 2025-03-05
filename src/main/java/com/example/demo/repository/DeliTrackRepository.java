package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.DeliTrack;
import com.example.demo.entity.Delivery;

public interface DeliTrackRepository extends JpaRepository<DeliTrack, Long> {
	List<DeliTrack> findByDeliveryOrderByUpdatedAtAsc(Delivery delivery);

	List<DeliTrack> findByDelivery(Delivery delivery);
}
