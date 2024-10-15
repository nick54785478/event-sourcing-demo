package com.example.demo.domain.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "SNAPSHOT")
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
	@Column(name = "CLASS_TYPE")
	private String classType;
	
	@Column(name = "AGGREGATE_ID")
	private String aggregateId;
	
	@Column(name = "BODY")
	private String state;
    
	@Column(name = "VERSION")
	private long version;

}