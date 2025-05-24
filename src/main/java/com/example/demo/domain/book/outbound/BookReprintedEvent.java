package com.example.demo.domain.book.outbound;

import com.example.demo.base.kernel.domain.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookReprintedEvent extends BaseEvent {

	@Getter
	private BookReprintedEventData data;

}
