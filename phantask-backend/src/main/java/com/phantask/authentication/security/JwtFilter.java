package com.phantask.authentication.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phantask.authentication.service.api.IUserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Servlet filter that inspects incoming HTTP requests for JWT tokens and sets
 * authentication.
 *
 * <p>
 * This filter:
 * <ol>
 * <li>Extracts the token from the Authorization header (typically "Bearer
 * &lt;token&gt;")</li>
 * <li>Validates the token using {@link JwtUtils}</li>
 * <li>If valid, loads user details and sets the Spring Security
 * {@code Authentication} in the context</li>
 * </ol>
 * </p>
 *
 * <p>
 * Implement as a OncePerRequestFilter so the token is processed exactly once
 * per request.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final IUserService userService;

	/**
	 * Extract and validate token from the request, and populate SecurityContext on
	 * success.
	 *
	 * @param request     the incoming HTTP request
	 * @param response    the HTTP response
	 * @param filterChain the remaining filter chain
	 * @throws ServletException on general servlet error
	 * @throws IOException      on I/O error
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException, java.io.IOException {

		String path = req.getServletPath();
		
		// SKIP JWT VALIDATION FOR PUBLIC ENDPOINTS
		if (path.startsWith("/api/auth/") || 
		    path.equals("/api/users/change-password-first-login") ||
		    path.equals("/api/users/update-profile-first-login")) {
			chain.doFilter(req, res);
			return;
		}

		String header = req.getHeader("Authorization");
		String username = null;
		String token = null;

		// Extract token from Authorization header
		if (header != null && header.startsWith("Bearer ")) {
			token = header.substring(7);
			try {
				username = jwtUtil.extractUsername(token);
			} catch (ExpiredJwtException e) {
				// Token expired - send 401 and stop processing
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json");
				res.getWriter().write("{\"error\": \"Token expired, please login again\"}");
				return;
			} catch (Exception e) {
				// Invalid token - send 401 and stop processing
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json");
				res.getWriter().write("{\"error\": \"Invalid token\"}");
				return;
			}
		}

		// If we have a valid username from the token, authenticate the user
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				UserDetails userDetails = userService.loadUserByUsername(username);
				
				// Validate token and set authentication
				if (jwtUtil.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null, 
						userDetails.getAuthorities()
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				} else {
					// Token validation failed - send 403
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.setContentType("application/json");
					res.getWriter().write("{\"error\": \"Invalid or expired token\"}");
					return;
				}
			} catch (Exception e) {
				// User not found or other error - send 403
				res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				res.setContentType("application/json");
				res.getWriter().write("{\"error\": \"Authentication failed\"}");
				return;
			}
		}
		
		// Continue with the filter chain
		chain.doFilter(req, res);
	}
}
