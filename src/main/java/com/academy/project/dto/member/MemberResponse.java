package com.academy.project.dto.member;

import com.academy.project.dto.response.UserResponse;
import com.academy.project.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberResponse {

    private Long id;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private List<String> subscribedCourses;

    public static MemberResponse fromUser(User user, List<String> subscribedCourses) {
        return MemberResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .subscribedCourses(subscribedCourses)
                .build();
    }

    public static MemberResponse fromUserResponse(UserResponse user) {
        return MemberResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
