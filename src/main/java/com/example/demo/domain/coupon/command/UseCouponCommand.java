package com.example.demo.domain.coupon.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CouponCommand
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseCouponCommand {

	private String couponNo;
	private String usedTo;

}
