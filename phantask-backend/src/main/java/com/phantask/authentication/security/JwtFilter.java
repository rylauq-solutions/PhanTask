package com.phantask.authentication.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
 * <li>Validates the token using {@link JwtUtil}</li>
 * <li>Extracts roles from the JWT token and loads them as authorities</li>
 * <li>Creates both ROLE_* and non-prefixed authorities for flexibility</li>
 * <li>If valid, loads user details and sets the Spring Security
 * {@code Authentication} in the context with JWT-derived authorities</li>
 * </ol>
 * </p>
 *
 * <p>
 * Note: This filter creates two versions of each authority:
 * - Plain version (e.g., "ADMIN") for use with hasAuthority()
 * - ROLE_ prefixed version (e.g., "ROLE_ADMIN") for use with hasRole()
 * This allows controllers to use either hasRole() or hasAuthority() interchangeably.
 * </p>
 *
 * <p>
 * Implemented as a OncePerRequestFilter so the token is processed exactly once
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
		
		// Skip JWT validation for public endpoints
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
				// Extract roles from JWT token claims
				List<String> rolesFromJwt = jwtUtil.extractRoles(token);
				
				// Create authorities with both plain and ROLE_ prefixed versions
				// This allows using both hasAuthority('ADMIN') and hasRole('ADMIN')
				List<GrantedAuthority> authorities = new ArrayList<>();
				for (String role : rolesFromJwt) {
					// Add plain authority (for hasAuthority)
					authorities.add(new SimpleGrantedAuthority(role));
					
					// Add ROLE_ prefixed authority (for hasRole)
					if (!role.startsWith("ROLE_")) {
						authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
					}
				}
				
				// Load user details for token validation
				UserDetails userDetails = userService.loadUserByUsername(username);
				
				// Validate token against user details
				if (jwtUtil.isTokenValid(token, userDetails)) {
					
					// Create authentication token with authorities from JWT
					// Note: Contains both plain and ROLE_ prefixed authorities
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null, 
						authorities
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					
//					System.out.println("Authentication successful with authorities: " 
//							+ SecurityContextHolder.getContext().getAuthentication().getAuthorities());
				} else {
					// Token validation failed - send 403
					System.err.println("Token validation failed for user: " + username);
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.setContentType("application/json");
					res.getWriter().write("{\"error\": \"Invalid or expired token\"}");
					return;
				}
			} catch (Exception e) {
				// User not found or other authentication error - send 403
				System.err.println("Authentication error: " + e.getMessage());
				e.printStackTrace();
				res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				res.setContentType("application/json");
				res.getWriter().write("{\"error\": \"Authentication failed: " + e.getMessage() + "\"}");
				return;
			}
		}
		
		// Continue with the filter chain
		chain.doFilter(req, res);
	}
}
