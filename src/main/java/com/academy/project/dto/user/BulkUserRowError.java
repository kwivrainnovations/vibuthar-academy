package com.academy.project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserRowError {

    /** 1-based Excel row number. */
    private int row;
    private String message;

    /** Original uploaded values (for error-report Excel / fix-and-retry). */
    private String fullName;
    private String mobileNumber;
    private String password;
    private String role;
}
