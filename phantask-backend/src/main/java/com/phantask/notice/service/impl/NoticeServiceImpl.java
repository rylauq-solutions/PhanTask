package com.phantask.notice.service.impl;

import com.phantask.notice.dto.CreateNoticeDTO;
import com.phantask.notice.dto.NoticeResponse;
import com.phantask.notice.entity.Notice;
import com.phantask.notice.entity.NoticePriority;
import com.phantask.notice.repository.NoticeRepository;
import com.phantask.notice.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {

	private final NoticeRepository noticeRepository;

	public NoticeServiceImpl(NoticeRepository noticeRepository) {
		this.noticeRepository = noticeRepository;
	}

	// Helper: convert entity to response
	private NoticeResponse toResponse(Notice n) {
		NoticeResponse r = new NoticeResponse();
		r.setId(n.getId());
		r.setTitle(n.getTitle());
		r.setContent(n.getContent());
		r.setPostedBy(n.getPostedBy());
		r.setPriority(n.getPriority() == null ? null : n.getPriority().name());
		r.setTargetRoles(n.getTargetRoles());
		r.setCreatedAt(n.getCreatedAt());
		return r;
	}

	@Override
	public NoticeResponse createNotice(CreateNoticeDTO dto) {
		Notice notice = new Notice();
		notice.setTitle(dto.getTitle());
		notice.setContent(dto.getContent());
		notice.setPostedBy(dto.getPostedBy());
		notice.setPriority(NoticePriority.valueOf(dto.getPriority()));
		notice.setTargetRoles(dto.getTargetRoles());
		notice.setCreatedAt(LocalDateTime.now());

		Notice saved = noticeRepository.save(notice);
		return toResponse(saved);
	}

	@Override
	public NoticeResponse updateNotice(Long id, CreateNoticeDTO dto) {
		Notice notice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("Notice not found"));

		notice.setTitle(dto.getTitle());
		notice.setContent(dto.getContent());
		notice.setPostedBy(dto.getPostedBy());
		notice.setPriority(NoticePriority.valueOf(dto.getPriority()));
		notice.setTargetRoles(dto.getTargetRoles());

		Notice saved = noticeRepository.save(notice);
		return toResponse(saved);
	}

	@Override
	public boolean deleteNotice(Long id) {
		if (noticeRepository.existsById(id)) {
			noticeRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public List<NoticeResponse> getAllNoticesAdmin() {
		return noticeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	public List<NoticeResponse> getAllNoticesForUser(List<String> roles) {
		if (roles == null || roles.isEmpty()) {
			return List.of();
		}

		return noticeRepository.findByTargetRolesIn(roles).stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	public List<NoticeResponse> getNoticesByPriorityForUser(List<String> roles, String priority) {
		if (roles == null || roles.isEmpty()) {
			return List.of();
		}

		NoticePriority noticePriority = NoticePriority.valueOf(priority);
		return noticeRepository.findByTargetRolesInAndPriority(roles, noticePriority).stream().map(this::toResponse)
				.collect(Collectors.toList());
	}
}
