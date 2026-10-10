package com.academy.project.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class BulkUserParseResult {

    private int totalRows;
    @Builder.Default
    private List<BulkCreateUserItem> validItems = Collections.emptyList();
    @Builder.Default
    private List<BulkUserRowError> errors = Collections.emptyList();
}
