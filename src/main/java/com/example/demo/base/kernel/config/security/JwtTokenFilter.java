package com.example.demo.base.kernel.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.base.core.application.JwtTokenApplicationService;
import com.example.demo.base.kernel.config.context.ContextHolder;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用於攔截所有的 HTTP Request，從 Header 中提取授權資訊，將其轉換為 JWT（JSON Web Token）後， 存儲到
 * ContextHolder 內以供後續使用。
 */
@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenApplicationService jwtTokenService;

	@Value("${jwt.auth.enabled}")
	private boolean jwtAuthEnabled;

	/**
	 * 用於進行 URL 路徑匹配的 Ant 格式路徑匹配器。
	 */
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 公開的路徑列表，不需要進行 JWT 驗證的路徑。
	 */
	private static final String[] PUBLIC_PATHS = { "/health", "/favicon.ico", "**/swagger-ui**", "**/swagger-ui/**",
			"/api-docs/**", "/train/**", "/account/**", "/actuator/**" };

	/**
	 * 過濾器的核心方法，用於處理 Request 中的授權資訊。
	 * 
	 * @param request  HTTP 請求
	 * @param response HTTP 回應
	 * @param chain    過濾器鏈
	 * @throws ServletException 如果發生 Servlet 相關異常
	 * @throws IOException      如果發生 I/O 相關異常
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		log.debug("AuthenticationTokenFilter doFilterInternal");

		// 未開直接放行
		if (!jwtAuthEnabled) {
			// 測試用: 10 年的 Token
			String token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJEQVRBX09XTkVSIl0sImlzcyI6IlNZU1RFTSIsInN1YiI6Im5pY2sxMjNAZXhhbXBsZS5jb20iLCJpYXQiOjE3MjYxMzcyNDQsImV4cCI6MjA0MTQ5NzI0NH0.kxzrgtEifDx7u-IFAl9lHQshyjUYOaHkRfCi7ZWFooY";
			Claims tokenBody = jwtTokenService.getTokenBody(token);
			ContextHolder.setJwtClaims(tokenBody);
			ContextHolder.setJwtToken(token);
			chain.doFilter(request, response);
			return;
		}

		// 如果該 Request 不需要 JWT 驗證，則直接放行
		if (!requiresJwtValidation(request)) {
			chain.doFilter(request, response);
			return;
		}

		// 從 Request Header 中獲取授權資訊
		String authorization = request.getHeader(JwtConstants.JWT_HEADER.getValue());
		log.debug("request header authorization: {}", authorization);

		// 如果授權資訊不為空且以 Bearer 開頭
		if (authorization != null && authorization.startsWith(JwtConstants.JWT_PREFIX.getValue())) {
			// 截取 Bearer 後面的 Access Token
			final String token = authorization.substring(JwtConstants.JWT_PREFIX.getValue().length());

			if (validateJwtToken(token, response)) {
				chain.doFilter(request, response);
			}

		} else {
			// 如果沒有獲取到授權資訊，返回 401 錯誤碼
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No JWT Token");
		}
	}

	/**
	 * 根據 Request 的 URL 判斷是否需要進行 JWT 驗證。
	 * 
	 * @param request HTTP 請求
	 * @return 是否需要進行 JWT 驗證
	 */
	private boolean requiresJwtValidation(HttpServletRequest request) {
		String apiPrefix = "/api/v1";
		String requestPath = request.getRequestURI();
		// 檢查 Request 的 URL 是否在公開路徑列表中，如果是則不需要進行 JWT 驗證
		for (String publicPath : PUBLIC_PATHS) {
			log.info("publicPath:{}, requestPath:{}", publicPath, requestPath);
			if (pathMatcher.match(apiPrefix + publicPath, requestPath)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 驗證 JWT Token，並在驗證成功後將 Token 存儲到 ContextHolder 中。
	 * 
	 * @param token    JWT Token
	 * @param response HTTP 回應
	 * @return 驗證結果，驗證成功返回 true，否則返回 false
	 * @throws IOException 如果發生 I/O 相關異常
	 */
	private boolean validateJwtToken(String token, HttpServletResponse response) throws IOException {
		// 解析 JWT Token
		Claims claims = jwtTokenService.getTokenBody(token);

		log.debug("JWT claims: {}", claims);

		if (jwtAuthEnabled) {
			// 驗證 JWT Token
			jwtTokenService.parseToken(token);
		}

		// 驗證通過將 JWT Token 存儲到 ContextHolder 中
		ContextHolder.setJwtToken(token);
		ContextHolder.setJwtClaims(claims);

		return true;

	}

}
