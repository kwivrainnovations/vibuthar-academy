package com.academy.project.controller.admin;

import com.academy.project.dto.intrest.InterestResponse;
import com.academy.project.dto.member.MemberResponse;
import com.academy.project.dto.response.ApiResponse;
import com.academy.project.dto.response.PagedResponse;
import com.academy.project.dto.user.BulkUserErrorReportRequest;
import com.academy.project.dto.user.BulkUserUploadResponse;
import com.academy.project.enums.EmailStatus;
import com.academy.project.service.intrest.InterestService;
import com.academy.project.service.member.MemberService;
import com.academy.project.service.user.UserService;
import com.academy.project.util.UserExcelHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminMemberController {

    private final InterestService interestService;
    private final MemberService memberService;
    private final UserService userService;

    @GetMapping("/interests")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<ApiResponse<PagedResponse<InterestResponse>>> listInterestedCandidates(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseOfInterest,
            @RequestParam(required = false) EmailStatus emailStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<InterestResponse> response = interestService.listInterests(
                search, courseOfInterest, emailStatus, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok("Interested candidates fetched successfully", response));
    }

    @GetMapping("/members/subscribed")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<ApiResponse<PagedResponse<MemberResponse>>> listSubscribedMembers(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<MemberResponse> response = memberService.listSubscribedMembers(
                courseId, search, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok("Subscribed members fetched successfully", response));
    }

    @GetMapping("/members/non-subscribed")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<ApiResponse<PagedResponse<MemberResponse>>> listNonSubscribedMembers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<MemberResponse> response = memberService.listNonSubscribedMembers(
                search, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok("Non-subscribed members fetched successfully", response));
    }

    /** Download empty/sample Excel template for bulk user upload. */
    @GetMapping("/users/sample-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadSampleUsersExcel() {
        byte[] file = userService.downloadSampleUsersExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + UserExcelHelper.SAMPLE_FILENAME + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    /** Upload filled Excel to add users in bulk. Invalid rows are skipped; valid rows are created. */
    @PostMapping(value = "/users/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkUserUploadResponse>> importUsersFromExcel(
            @RequestParam("file") MultipartFile file) {
        BulkUserUploadResponse response = userService.importUsersFromExcel(file);
        String message;
        if (response.getFailedCount() == 0) {
            message = "Users imported from Excel successfully";
        } else if (response.getCreatedCount() == 0) {
            message = "No users imported. Fix the row errors and retry.";
        } else {
            message = "Bulk upload completed with some row errors";
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, response));
    }

    /**
     * Download Excel of failed bulk-upload rows.
     * Pass the {@code errors} array from the upload response body.
     */
    @PostMapping("/users/excel/error-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadBulkUserErrorReport(
            @Valid @RequestBody BulkUserErrorReportRequest request) {
        byte[] file = userService.downloadBulkUserErrorReport(request.getErrors());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + UserExcelHelper.ERROR_REPORT_FILENAME + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
    }
}
