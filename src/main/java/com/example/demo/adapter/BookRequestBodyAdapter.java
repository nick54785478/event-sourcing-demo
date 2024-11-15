package com.example.demo.adapter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.example.demo.domain.book.command.CreateBookCommand;
import com.example.demo.domain.book.command.UpdateBookCommand;
import com.example.demo.iface.dto.CreateBookResource;
import com.example.demo.util.BaseDataTransformer;

import lombok.extern.slf4j.Slf4j;

/**
 * Book Controller Request Adapter 示範
 * 目前是採用 BaseDataTransformer 直接轉
 * 暫不使用
 */
@Slf4j
// 註解掉 目前暫不使用
//@RestControllerAdvice(assignableTypes = { BookController.class })
public class BookRequestBodyAdapter extends RequestBodyAdviceAdapter {

	/**
	 * 設置 Command 進執行緒
	 */
	private static final ThreadLocal<Map<Class<?>, Object>> commandThreadLocal = ThreadLocal.withInitial(HashMap::new);

	/**
	 * 取得 Command
	 */
	public static <T> T getCurrentCommand(Class<T> clazz) {
		return clazz.cast(commandThreadLocal.get().get(clazz));
	}

	/**
	 * 清除執行緒的 Command
	 */
	public static void clear() {
		commandThreadLocal.remove();
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		// 這裡可以設定對哪些類型的請求進行攔截處理
		log.info("執行防腐處理，進行資料轉換");
		// 這裡檢查目標類型是否為 特定類型，決定是否攔截
		return targetType == CreateBookResource.class;
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {

		Object command = null;
		
		// 進行防腐處理執行轉換邏輯，將 Resource 轉為 Command
		if (body instanceof CreateBookResource) {
			command = BaseDataTransformer.transformData(body, CreateBookCommand.class);
		} else if (body instanceof UpdateBookCommand) {
			command = BaseDataTransformer.transformData(body, UpdateBookCommand.class);
		}
		// 將 Command 設置進執行緒供後欲續取用
		commandThreadLocal.get().put(command.getClass(), command);
		return body;

	}


}
