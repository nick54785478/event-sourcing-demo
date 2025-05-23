package com.example.demo.base.kernel.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 物件解析工具
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperUtil {

	protected static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new JavaTimeModule());
	}

	/**
	 * 序列化物件 為 JSON 字串
	 * 
	 * @param target 目標物件
	 * @return String 序列化後的 json 字串
	 */
	public static String serialize(Object target) {
		try {
			return mapper.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			log.error("Occurred Json Processing Exception", e);
			return "";
		}
	}

	/**
	 * 序列化物件 為 byte[]
	 * 
	 * @param target 目標物件
	 * @return 物件序列化後的 byte[]
	 */
	public static byte[] serializeAsBytes(Object target) {
		try {
			return mapper.writeValueAsBytes(target);
		} catch (JsonProcessingException e) {
			log.error("Occurred Json Processing Exception", e);
			return new byte[0];
		}
	}

	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target byte[]
	 * @param clazz  目標物件類型
	 * @return 物件
	 */
	public static <T> T unserialize(byte[] target, Class<T> clazz) {
		try {
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (IOException e) {
			log.error("Occurred ByteProcessing Exception", e);
			return null;
		}
	}

	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target json 字串
	 * @param clazz  目標物件類型
	 * @return 物件
	 */
	public static <T> T unserialize(String target, Class<T> clazz) {
		try {
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}

	/**
	 * 反序列化 JSON 回 物件列表
	 * 
	 * @param target json 字串
	 * @param clazz  目標物件類型
	 * @return 物件列表
	 */
	public static <T> List<T> unserializeArrayOfObject(String target, Class<T> clazz) {
		try {
			return mapper.readValue(target, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}

	/**
	 * 將物件展平為 Map<String, Object>
	 * 
	 * @param target 目標物件
	 * @return Map<物件欄位, 值>
	 */
	public static Map<String, Object> convertToMap(Object target) {
		return mapper.convertValue(target, new TypeReference<>() {
		});
	}

	/**
	 * 將 JSON（字串）內容合併到現有物件上，僅更新指定欄位。
	 */
	public static <T> T mergeJsonIntoObject(T target, String jsonStr) {
		try {
			return mapper.readerForUpdating(target).readValue(jsonStr);
		} catch (Exception e) {
			throw new RuntimeException("合併 JSON 到物件時發生錯誤", e);
		}
	}
}
