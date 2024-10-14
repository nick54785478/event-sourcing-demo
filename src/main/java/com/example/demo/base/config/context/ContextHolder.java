package com.example.demo.base.config.context;

import com.example.demo.base.config.security.JwtConstants;
import com.example.demo.base.event.BaseEvent;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

/**
 * 上下文工具類
 */
@Slf4j
public class ContextHolder {

	/**
	 * JWT Claims 的 Key，用於指定 JWT 的 Subject
	 */
	public static final String CLAIM_KEY_SUB = "sub";

	/**
	 * JWT Claims 的 Key，用於指定 JWT 的 Acc
	 */
	public static final String CLAIM_KEY_ACC = "acc";

	/**
	 * 儲存從 JWT Token 解析出來的 Claims 內容
	 */
	private static final ThreadLocal<Claims> JWT_CLAIMS = new ThreadLocal<>();

	/** 儲存目前使用者傳入的 JWT Token */
	private static final ThreadLocal<String> JWT_TOKEN = new ThreadLocal<>();

	/**
	 * 儲存 Event
	 */
	private static final ThreadLocal<BaseEvent> EVENT = new ThreadLocal<>();

	/**
	 * 將 JWT Claims 設定到 ThreadLocal 內。
	 * 
	 * @param claims JWT ClaimsSet
	 */
	public static void setJwtClaims(Claims claims) {
		log.debug("ContextHolder setJwtClaim {}", claims);
		JWT_CLAIMS.set(claims);
	}

	/**
	 * 把 JWT Token 設定到 ThreadLocal 內
	 * 
	 * @param claims
	 */
	public static void setJwtToken(String token) {
		JWT_TOKEN.set(token);
	}

	/**
	 * 把 BaseEvent 設定到 ThreadLocal 內
	 * 
	 * @param event
	 */
	public static void setEvent(BaseEvent event) {
		EVENT.set(event);
	}

	/**
	 * 取得目前登入者的用戶帳號
	 * 
	 * @return 目前登入者的用戶帳號
	 */
	public static String getUsername() {
		return JWT_CLAIMS.get() != null ? JWT_CLAIMS.get().getSubject() : null;
	}

	/**
	 * 取得目前登入者的用戶信箱
	 * 
	 * @return 目前登入者的用戶信箱
	 */
	public static String getUserEmail() {
		return JWT_CLAIMS.get() != null ? (String) JWT_CLAIMS.get().get(JwtConstants.JWT_CLAIMS_KEY_EMAIL.getValue())
				: null;
	}

	/**
	 * 取得目前登入者的 JwToken
	 * 
	 * @return token
	 */
	public static String getJwtoken() {
		return JWT_TOKEN.get() != null ? JWT_TOKEN.get() : null;
	}

	/**
	 * 獲取事件
	 * 
	 * @return Event 資訊
	 */
	public static BaseEvent getEvent() {
		return EVENT.get() != null ? EVENT.get() : null;
	}

	/**
	 * 清理上下文
	 */
	public static void clearContext() {
		EVENT.remove();
		JWT_CLAIMS.remove();
		JWT_TOKEN.remove();
	}

}
