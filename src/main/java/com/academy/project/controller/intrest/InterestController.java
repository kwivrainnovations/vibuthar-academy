package com.academy.project.controller.intrest;

import com.academy.project.dto.intrest.InterestRequest;
import com.academy.project.dto.intrest.InterestResponse;
import com.academy.project.service.intrest.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interests")
@RequiredArgsConstructor
@CrossOrigin

public class InterestController {

    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestResponse> createInterest(
            @Validated @RequestBody InterestRequest request) {

        InterestResponse response =
                interestService.createInterest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}