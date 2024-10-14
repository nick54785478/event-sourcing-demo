package com.example.demo.domain.coupon.aggregate;

import java.util.List;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.coupon.command.UseCouponCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "BOOK_COUPON")
@NoArgsConstructor
public class Coupon extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // PK id

	@Column(name = "COUPON_NO")
	private String couponNo; // Aggregate Identifier

	@Column(name = "TOKEN")
	private String token; // token

	@Column(name = "USED_TO")
	private String usedTo; // used_to

	// Coupon 是否使用過
	public void checkUse(List<Object> arguments) {
		if (this.usedTo != null)
			throw new ValidationException("VALIDATED_FAIL", "Coupon is already used");
	}
	
	// 使用 Coupon
	public void use(UseCouponCommand command) {
		this.usedTo = command.getUsedTo();
	}
}
