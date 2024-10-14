package com.example.demo.domain.book.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Command
 * 		使用者(或軟體)所做出的決定，從系統角度來看，Command也可能是我們要實作功能的行為。
 * */

/**
 * 新增用的Command 傳參數給Application Service用的JavaBean
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookCommand {
	private String name;
	private String author;
	private String isbn;
	private String coupon; // coupon
	private String couponNo; // couponId

}
