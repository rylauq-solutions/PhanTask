package com.phantask.notice.service;

import com.phantask.notice.dto.CreateNoticeDTO;
import com.phantask.notice.dto.NoticeResponse;
import java.util.List;

public interface NoticeService {

    // Admin operations
    NoticeResponse createNotice(CreateNoticeDTO dto);
    NoticeResponse updateNotice(Long id, CreateNoticeDTO dto);
    boolean deleteNotice(Long id);
    List<NoticeResponse> getAllNoticesAdmin();

    // User operations (filtered by roles)
    List<NoticeResponse> getAllNoticesForUser(List<String> roles);
    List<NoticeResponse> getNoticesByPriorityForUser(List<String> roles, String priority);
}
