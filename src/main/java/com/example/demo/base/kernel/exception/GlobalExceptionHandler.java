package com.example.demo.base.kernel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.base.core.util.BaseDataTransformer;
import com.example.demo.base.kernel.exception.response.BaseExceptionResponse;

/**
 * 全域例外處理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<BaseExceptionResponse> handleValidationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseDataTransformer.transformData(e, BaseExceptionResponse.class));
	}

}
