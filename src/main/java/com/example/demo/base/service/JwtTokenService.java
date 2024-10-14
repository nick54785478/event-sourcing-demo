package com.example.demo.base.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.base.config.security.JwtConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用於執行 Jwt 相關操作的 Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenService {

	@Value("${jwt.secret.key}")
	private String scretKey;

	/**
	 * 從 token 中取得使用者名稱
	 * 
	 * @param token
	 * @return 使用者名稱
	 */
	public String getUsername(String token) {
		log.info("getUsername: " + getTokenBody(token).getSubject());
		log.info("TokenBody: " + getTokenBody(token));
		return getTokenBody(token).getSubject();
	}

	/**
	 * 取得使用者信箱
	 * 
	 * @param token
	 * @return 使用者信箱
	 */
	public String getEmail(String token) {
		return (String) getTokenBody(token).get(JwtConstants.JWT_CLAIMS_KEY_EMAIL.getValue());
	}

	/**
	 * 取得使用者角色
	 * 
	 * @param token
	 * @return 使用者角色
	 */
	public List<String> getRoleList(String token) {
		return (List<String>) getTokenBody(token).get(JwtConstants.JWT_CLAIMS_KEY_ROLE.getValue());
	}

	/**
	 * 取得簽發日
	 * 
	 * @param token
	 * @return 簽發日
	 */
	public Date getIssAt(String token) {
		return getTokenBody(token).getIssuedAt();
	}

	/**
	 * 取得過期日
	 * 
	 * @param token
	 * @return 過期日
	 */
	public Date getExpDate(String token) {
		return getTokenBody(token).getExpiration();
	}

	/**
	 * 是否已過期
	 * 
	 * @param token
	 * @return true/false
	 */
	public boolean isExpiration(String token) {
		return getTokenBody(token).getExpiration().before(new Date());
	}

	/**
	 * 取得 Token 主體
	 * 
	 * @param token
	 * @return Claims
	 */
	public Claims getTokenBody(String token) {
		// 使用 Jwts.parser() 建立 JwtParser 實例
		return Jwts.parser().verifyWith(getSigningKey()) // 用於設定金鑰，該金鑰用於驗證 JWT 令牌的簽名
				.build() // 建置 JwtParser 實例
				.parseSignedClaims(token).getPayload();
	}

	/**
	 * 解析 Token
	 * 
	 * @param token
	 * @return 解析後的結果 Map
	 */
	public Map<String, Object> parseToken(String token) {
		Claims claims = getTokenBody(token);
		return claims.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * 取得 SigningKey
	 * 
	 * @return key
	 */
	private SecretKey getSigningKey() {
		log.info("Secret Key: {}", scretKey);
		byte[] keyBytes = Decoders.BASE64.decode(scretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * 驗證 JWToken 合法性
	 * 
	 * @param token
	 * @return true/false
	 */
	public boolean validateToken(String token) {
		Claims claims = this.getTokenBody(token);
		return claims != null;
	}

}
