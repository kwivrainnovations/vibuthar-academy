package com.academy.project.service.intrest;

import com.academy.project.dto.intrest.InterestRequest;
import com.academy.project.dto.intrest.InterestResponse;
import com.academy.project.dto.response.PagedResponse;
import com.academy.project.enums.EmailStatus;

public interface InterestService {

    InterestResponse createInterest(InterestRequest request);

    PagedResponse<InterestResponse> listInterests(
            String search,
            String courseOfInterest,
            EmailStatus emailStatus,
            int page,
            int size
    );
}
