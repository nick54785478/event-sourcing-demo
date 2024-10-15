package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.snapshot.Snapshot;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {

}
