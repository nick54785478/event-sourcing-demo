package com.example.demo.domain.book.outbound;

import org.modelmapper.AbstractConverter;

import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.domain.coupon.command.UseCouponCommand;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/*
*  事件(Event): 
*  	已經發生過的事，其特徵有:	
*		1. 橘色便利貼(Event Storming中使用)。
*		2. 過去式，代表著系統的狀態。
*		3. 領域專家所在乎的事件。
*		4. 有時間性(時間軸概念，表示有先後順序)。
*		5. 可加上時間概念(何時已完成)。
*		6. 原因。
* */

/**
 * 新增事件
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookStoredEvent extends BaseEvent {

	@Getter
	private BookStoredEventData data;

	public static AbstractConverter<BookStoredEvent, UseCouponCommand> getConverter() {

		return new AbstractConverter<BookStoredEvent, UseCouponCommand>() {
			protected UseCouponCommand convert(BookStoredEvent source) {
				if (source == null) {
					return null;
				}

				// 呼叫 Command
				UseCouponCommand target = new UseCouponCommand();

				// 若是存在TargetId，將其設置入UseCouponCommand
				if (source.getTargetId() != null)
					target.setUsedTo(source.getTargetId());

				// 若是存在CouponId，將其設置入UseCouponCommand
				if (source.getData().getCouponNo() != null)
					target.setCouponNo(source.getData().getCouponNo());
				return target;
			}
		};
	}
}
