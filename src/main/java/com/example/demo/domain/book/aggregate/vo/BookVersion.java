package com.example.demo.domain.book.aggregate.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Value Object 描述某一個事物的特徵。
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "BOOK_VERSION")
public class BookVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // id

	@Column(name = "BOOK_UUID")
	private String bookUuid; // book_uuid

	@Column(name = "VERSION")
	private Integer version; // version
	
	public BookVersion(String bookUuid, Integer version) {
		this.bookUuid = bookUuid;
		this.version = version;
	}
}
