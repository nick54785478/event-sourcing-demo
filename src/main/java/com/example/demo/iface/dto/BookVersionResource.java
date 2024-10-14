package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resource class for the Command API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookVersionResource {

	private Integer version;

}
