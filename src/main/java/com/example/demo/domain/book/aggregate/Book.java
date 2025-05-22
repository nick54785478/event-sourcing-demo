package com.example.demo.domain.book.aggregate;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.example.demo.base.core.domain.BaseAggregateRoot;
import com.example.demo.base.kernel.config.context.ContextHolder;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.RenameBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.book.outbound.BookStoredEvent;
import com.example.demo.domain.book.outbound.BookStoredEventData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BOOK")
public class Book extends BaseAggregateRoot {

	@Id
	@Column(name = "UUID")
	private String uuid; // PK uuid

	@Transient
	private String u;

	@Column(name = "NAME")
	private String name; // 姓名

	@Column(name = "AUTHOR")
	private String author; // 作者

	@Column(name = "ISBN")
	private String isbn; // isbn
	
	@Column(name = "VERSION")
	private Integer version = 0;

	/**
	 * 在持久化之前執行的方法，用於設置 Train UUID。
	 */
	@PrePersist
	public void prePersist() {
		if (Objects.isNull(this.uuid)) {
			this.uuid = this.u;
		}
	}

	/**
	 * Constructor Command Handler. registers Event
	 */
	public void create(CreateBookCommand command) {
		this.u = UUID.randomUUID().toString();
		this.name = command.getName();
		this.author = command.getAuthor();
		this.isbn = command.getIsbn();
		this.version += 1;

		// 設置CouponNo
		String couponNo = (command.getCouponNo() != null) ? command.getCouponNo() : null;

		// 註冊 Domain Event（當有 Next Event 需要發佈時）
		BaseEvent event = BookStoredEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.u) // 呼叫新增事件
				.data(new BookStoredEventData(this.u, couponNo)).build();

		ContextHolder.setEvent(event);
	}

	/**
	 * 更新 Book 資料
	 * 
	 * @param command
	 */
	public void update(UpdateBookCommand command) {
		this.name = command.getName();
		this.author = command.getAuthor();
		this.isbn = command.getIsbn();
		this.version += 1;

		// 註冊 Domain Event（當有 Next Event 需要發佈時）
		BaseEvent event = BookStoredEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.uuid)
				.data(new BookStoredEventData(this.uuid)).build();
		ContextHolder.setEvent(event);
	}


	/**
	 * replay EventSourcing
	 * 
	 * @param command
	 */
	public void replay(ReplayBookCommand command) {
		this.uuid = command.getUuid();
		this.u = command.getU();
		this.name = command.getName();
		this.author = command.getAuthor();
		this.isbn = command.getIsbn();
		this.version = command.getVersion();
		this.setCreatedDate(command.getCreatedDate());
		this.setCreatedBy(command.getCreatedBy());
	}

	/**
	 * rename
	 * 
	 * @param command
	 */
	public void rename(RenameBookCommand command) {
		if (StringUtils.isNotBlank(command.getName())) {
			this.name = command.getName();
			this.version += 1;

			// 註冊 Domain Event（當有 Next Event 需要發佈時）
			BaseEvent event = BookStoredEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.uuid)
					.data(new BookStoredEventData(this.uuid)).build();
			ContextHolder.setEvent(event);
		}
	}

}
