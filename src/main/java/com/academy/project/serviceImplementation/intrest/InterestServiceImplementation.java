package com.academy.project.serviceImplementation.intrest;

import com.academy.project.dto.intrest.InterestRequest;
import com.academy.project.dto.intrest.InterestResponse;
import com.academy.project.entity.intrest.Interest;
import com.academy.project.enums.EmailStatus;
import com.academy.project.repository.interest.InterestRepository;
import com.academy.project.service.intrest.InterestService;
import com.academy.project.service.emailService.IntrestEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestServiceImplementation implements InterestService {

    private final InterestRepository interestRepository;
    private final IntrestEmailService emailService;

    @Override
    public InterestResponse createInterest(InterestRequest request) {

        // 1. Save candidate in MySQL
        Interest interest = Interest.builder()
                .username(request.getUsername())
                .emailId(request.getEmailId())
                .description(request.getDescription())
                .courseOfInterest(request.getCourseOfInterest())
                .mobileNumber(request.getMobileNumber())
                .emailStatus(EmailStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Interest savedInterest = interestRepository.save(interest);

        // 2. Send email
        try {

            emailService.sendInterestNotification(savedInterest);

            savedInterest.setEmailStatus(EmailStatus.SENT);

        } catch (Exception exception) {

            savedInterest.setEmailStatus(EmailStatus.FAILED);

            // Log the actual error
            System.err.println(
                    "Failed to send interest email: "
                            + exception.getMessage()
            );
            savedInterest.setEmailStatus(EmailStatus.FAILED);
            log.error("Failed to send interest email for interestId={}: {}",
                    savedInterest.getId(), exception.getMessage(), exception);

        }

        // 3. Update email status
        Interest updatedInterest =
                interestRepository.save(savedInterest);

        return InterestResponse.builder()
                .id(updatedInterest.getId())
                .username(updatedInterest.getUsername())
                .emailId(updatedInterest.getEmailId())
                .courseOfInterest(updatedInterest.getCourseOfInterest())
                .emailStatus(updatedInterest.getEmailStatus())
                .createdAt(updatedInterest.getCreatedAt())
                .build();
    }
}
