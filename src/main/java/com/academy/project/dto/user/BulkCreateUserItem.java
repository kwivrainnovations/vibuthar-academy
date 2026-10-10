package com.academy.project.dto.user;

import com.academy.project.entity.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkCreateUserItem {

    /** 1-based Excel row number (for error messages). */
    private int sourceRow;

    private String name;
    private String phone;
    private String password;
    private UserRole role;
}
