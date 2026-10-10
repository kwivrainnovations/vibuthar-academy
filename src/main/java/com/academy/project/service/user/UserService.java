package com.academy.project.service.user;

import com.academy.project.dto.response.UserResponse;
import com.academy.project.dto.user.BulkUserRowError;
import com.academy.project.dto.user.BulkUserUploadResponse;
import com.academy.project.dto.user.UpdateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUserProfile();

    UserResponse updateUser(String userId, UpdateUserRequest request);

    void deleteUser(String userId);

    /** Empty/sample Excel template for bulk user upload. */
    byte[] downloadSampleUsersExcel();

    /** Import users from filled sample Excel. */
    BulkUserUploadResponse importUsersFromExcel(MultipartFile excelFile);

    /** Excel of failed bulk-upload rows for fix-and-retry. */
    byte[] downloadBulkUserErrorReport(List<BulkUserRowError> errors);
}
