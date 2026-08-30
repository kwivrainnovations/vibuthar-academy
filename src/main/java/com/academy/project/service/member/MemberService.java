package com.academy.project.service.member;

import com.academy.project.dto.member.MemberResponse;
import com.academy.project.dto.response.PagedResponse;

public interface MemberService {

    PagedResponse<MemberResponse> listSubscribedMembers(Long courseId, String search, int page, int size);

    PagedResponse<MemberResponse> listNonSubscribedMembers(String search, int page, int size);
}
