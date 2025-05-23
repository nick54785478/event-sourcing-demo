package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.book.aggregate.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

	void deleteByUuid(String uuid);

	Book findByUuid(String uuid);
}
