package com.phantask.notice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateNoticeDTO {
	private String title;
	private String content;
	private String postedBy;
	private String priority;
	private List<String> targetRoles;
}
