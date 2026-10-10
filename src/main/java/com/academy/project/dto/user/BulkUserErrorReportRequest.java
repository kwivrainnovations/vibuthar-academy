package com.academy.project.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkUserErrorReportRequest {

    @NotEmpty(message = "errors list is required")
    private List<BulkUserRowError> errors;
}
