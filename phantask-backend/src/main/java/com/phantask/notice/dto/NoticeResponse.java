package com.phantask.notice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeResponse {
	private Long id;
	private String title;
	private String content;
	private String postedBy;
	private String priority;
	private List<String> targetRoles;
	private LocalDateTime createdAt;
}
