package com.phantask.notice.controller;

import com.phantask.notice.dto.CreateNoticeDTO;
import com.phantask.notice.dto.NoticeResponse;
import com.phantask.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	// ----------------- ADMIN endpoints -----------------
	@PostMapping("/admin/create")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	public ResponseEntity<NoticeResponse> createNotice(@RequestBody CreateNoticeDTO dto) {
		NoticeResponse resp = noticeService.createNotice(dto);
		return ResponseEntity.ok(resp);
	}

	@PutMapping("/admin/update/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long id, @RequestBody CreateNoticeDTO dto) {
		NoticeResponse resp = noticeService.updateNotice(id, dto);
		return ResponseEntity.ok(resp);
	}

	@DeleteMapping("/admin/delete/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	public ResponseEntity<String> deleteNotice(@PathVariable Long id) {
		boolean deleted = noticeService.deleteNotice(id);

		if (deleted) {
			return ResponseEntity.ok("Notice deleted successfully");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notice not found");
		}
	}

	@GetMapping("/admin/all")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	public ResponseEntity<List<NoticeResponse>> adminAll() {
		return ResponseEntity.ok(noticeService.getAllNoticesAdmin());
	}

	// ----------------- USER endpoints -----------------
	// Helper to extract roles (without ROLE_ prefix)
	private List<String> getRolesFromAuth(Authentication auth) {
		if (auth == null)
			return Collections.emptyList();
		return auth.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", ""))
				.collect(Collectors.toList());
	}

	// Get all notices visible to logged-in user
	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<NoticeResponse>> myNotices(Authentication auth) {
		List<String> roles = getRolesFromAuth(auth);
		return ResponseEntity.ok(noticeService.getAllNoticesForUser(roles));
	}

	// Get notices by priority for logged-in user
	@GetMapping("/my/priority/{priority}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<NoticeResponse>> myNoticesByPriority(@PathVariable String priority,
			Authentication auth) {
		List<String> roles = getRolesFromAuth(auth);
		return ResponseEntity.ok(noticeService.getNoticesByPriorityForUser(roles, priority));
	}
}
