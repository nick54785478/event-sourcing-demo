package com.example.demo.infra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.book.aggregate.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

	void deleteByUuid(String uuid);

	Book findByUuid(String uuid);

	@Query("SELECT b FROM Book b WHERE b.version >= 10 AND MOD(b.version, 10) = 0")
	List<Book> findVersionGreaterThanEqual10AndDivisibleBy10();

}
