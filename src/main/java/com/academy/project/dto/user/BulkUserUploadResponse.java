package com.academy.project.dto.user;

import com.academy.project.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class BulkUserUploadResponse {

    private int totalRows;
    private int createdCount;
    private int failedCount;
    @Builder.Default
    private List<UserResponse> users = Collections.emptyList();
    @Builder.Default
    private List<BulkUserRowError> errors = Collections.emptyList();
}
