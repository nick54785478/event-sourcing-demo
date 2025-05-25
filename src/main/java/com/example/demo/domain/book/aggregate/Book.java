package com.example.demo.domain.book.aggregate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.core.domain.BaseAggregateRoot;
import com.example.demo.base.kernel.config.context.ContextHolder;
import com.example.demo.base.kernel.domain.event.BaseEvent;
import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.ReplayBookCommand.ReplayBookEventCommand;
import com.example.demo.domain.book.command.ReplayBookCommand.ReplayBookSnapshotCommand;
import com.example.demo.domain.book.command.ReprintBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.domain.book.outbound.BookCreatedEvent;
import com.example.demo.domain.book.outbound.BookCreatedEventData;
import com.example.demo.domain.book.outbound.BookReprintedEvent;
import com.example.demo.domain.book.outbound.BookReprintedEventData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
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
	 * 新增 Book 資料，註冊新增事件
	 * 
	 * @param command
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
		BaseEvent event = BookCreatedEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.u) // 呼叫新增事件
				.data(new BookCreatedEventData(this.u, couponNo)).build();
		ContextHolder.setEvent(event);
	}

	/**
	 * 更版 Book 資料，註冊更版事件
	 * 
	 * @param command
	 */
	public void reprint(ReprintBookCommand command) {
		this.name = command.getName();
		this.author = command.getAuthor();
		this.isbn = command.getIsbn();
		this.version += 1;

		// 註冊 Domain Event（當有 Next Event 需要發佈時）
		BaseEvent event = BookReprintedEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.uuid)
				.data(new BookReprintedEventData(this.uuid)).build();
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
	}

	/**
	 * 資料復原(從快照進行資料回復)
	 * 
	 * @param command
	 */
	public void recover(ReplayBookSnapshotCommand command) {
		this.uuid = command.getUuid(); // PK uuid
		this.name = command.getName(); // 姓名
		this.author = command.getAuthor(); // 作者
		this.isbn = command.getIsbn(); // isbn
		this.version = command.getVersion();
	}

	/**
	 * 將 Events 更新到 Book 中
	 * 
	 * @param commands
	 */
	public void apply(List<ReplayBookEventCommand> commands) {
		commands.stream().forEach(command -> {
			this.name = Objects.isNull(command.getName()) ? this.name : command.getName();
			this.author = Objects.isNull(command.getAuthor()) ? this.author : command.getAuthor();
			this.isbn = Objects.isNull(command.getIsbn()) ? this.isbn : command.getIsbn();
			this.version = command.getVersion();
		});
	}

}
