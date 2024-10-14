package com.example.demo.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.entity.EventSource;


@Repository
public interface EventSourceRepository extends JpaRepository<EventSource, Long> {

	EventSource findTopByUuidOrderByVersionDesc(String uuid);
	
}
