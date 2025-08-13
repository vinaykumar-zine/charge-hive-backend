package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.*;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminService {

    private final RestClient userClient;
    private final RestClient stationClient;
    private final RestClient bookingClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(RestClient.Builder restClientBuilder,AuditLogRepository auditLogRepository) {
        // Use the service name registered with Eureka, prefixed with "lb://"
        this.userClient = restClientBuilder.baseUrl("http://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("http://STATION-SERVICE").build();
        this.bookingClient = restClientBuilder.baseUrl("http://BOOKING-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

    private String getUsernameById(Long adminId) {
        try {
            // Attempt to retrieve the user DTO. If the user is not found,
            // .retrieve() will throw a WebClientResponseException.NotFound.
            AdminDto user = userClient.get()
                    .uri("auth/get-by-id/{id}", adminId)
                    .retrieve()
                    .body(AdminDto.class);

            if (user == null) {
                throw new UserNotFoundException("Received an empty response for admin ID: " + adminId);
            }

            return user.getName();

        } catch (WebClientResponseException e) {
            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
        }
    }


//    private String getUsernameById(Long adminId) {
//
//        ResponseEntity<AdminDto> responseEntity = userClient.get()
//                .uri("/api/users/{id}", adminId)
//                .retrieve()
//                .toEntity(AdminDto.class);
//
//        // userClient request doesn't return a responseEntity with null,
//        // it return an HTTP 404 Not Found status.
//        if (responseEntity.getStatusCode().is4xxClientError()) {
//            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
//        }
//
//        AdminDto user = responseEntity.getBody();
//
//        if (user != null) {
//            String username = user.getUsername();
//            if (username != null) {
//                return username;
//            } else {
//                throw new UserNotFoundException("Username not found for admin ID: " + adminId);
//            }
//        } else {
//            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
//        }
//    }

// --- station

    @Transactional
    public void approveOrRejectStation(Long userId, StationApprovalDto approvalDto) {
        //Call the Station Service to update the station
        //System.out.println(userId);
        log.info("userId = {}, approval DTO details: {}", userId, approvalDto);
        stationClient.put()
                .uri("/stations/update-status") // Assuming this is the endpoint in Station Service
                .body(approvalDto)
                .retrieve()
                .toBodilessEntity();

        //Create and save an audit log
        String action = approvalDto.isApproved() ? "APPROVE_STATION" : "REJECT_STATION";
        String details = "Station " + (approvalDto.isApproved() ? "approved" : "rejected") + " with reason: " + approvalDto.getReason();

        AuditLog log = AuditLog.builder()
                .adminUsername(getUsernameById(userId))
                .action(action)
                .targetEntity("Station")
                .targetId(approvalDto.getStationId())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<StationDto> getAllStations() {
        return stationClient.get()
                .uri("/stations")
                .retrieve()
                //telling station client that it has to convert json
                //to list of stationDto's
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    public List<StationDto> getUnapprovedStations() {
        return stationClient.get()
                .uri("/stations/unapproved")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    // --- User

    public List<UserDto> getAllUsers() {
        return userClient.get()
                .uri("/auth/get-all")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDto>>() {});
    }

    public UserDetailDto getUserDetials(Long userId) {

        try {
            // make synchronous call, handle 404 explicitly via onStatus
            UserDto userDto = userClient.get()
                    .uri("/get-by-id/{id}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // Response gives you access to status so you can inspect it
                        if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                            throw new UserNotFoundException(userId.toString());
                        }
                        throw new RuntimeException("User service returned 4xx: " + response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new RuntimeException("User service returned 5xx: " + response.getStatusCode()); })
                    .body(UserDto.class); // synchronous, blocking conversion

            if (userDto == null) {
                throw new RuntimeException("User service returned empty body for id: " + userId);
            }

            BookingResponseDto[] arr = bookingClient.get()
                    .uri("/bookings/{userId}",userId)
                    .retrieve()
                    .body(BookingResponseDto[].class);

            return UserDetailDto.builder()
                    .user(userDto)
                    .bookings(arr != null ? Arrays.asList(arr) : Collections.emptyList())
                    .build();

        } catch (UserNotFoundException e) {
            throw e; // propagate as 404
        } catch (RestClientResponseException ex) {
            // error response: we can read body if needed via ex.getResponseBodyAsString()
            throw new RuntimeException("User service error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            // network / IO / other client errors
            throw new RuntimeException("Failed to call user service", ex);
        }
    }


    // --- Admin Service ---

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }

    // --- Booking Management Methods ---

    /**
     * Get all bookings from booking service
     */
    public List<BookingResponseDto> getAllBookings() {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings")
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching all bookings", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get bookings by status
     */
    public List<BookingResponseDto> getBookingsByStatus(String status) {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/status/{status}", status)
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching bookings by status: {}", status, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get active bookings
     */
    public List<BookingResponseDto> getActiveBookings() {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/active")
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching active bookings", e);
            return Collections.emptyList();
        }
    }

    /**
     * Cancel a booking with admin override
     */
    public BookingResponseDto cancelBooking(Long adminId, Long bookingId, String reason) {
        try {
            BookingResponseDto booking = bookingClient.put()
                    .uri("/bookings/{bookingId}/cancel", bookingId)
                    .retrieve()
                    .body(BookingResponseDto.class);

            // Log the admin action
            String adminUsername = getUsernameById(adminId);
            AuditLog auditLog = AuditLog.builder()
                    .adminUsername(adminUsername)
                    .action("CANCEL_BOOKING")
                    .targetEntity("Booking")
                    .targetId(bookingId)
                    .details("Booking cancelled by admin" + (reason != null ? " - Reason: " + reason : ""))
                    .build();
            auditLogRepository.save(auditLog);

            return booking;
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", bookingId, e);
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }

    /**
     * Complete a booking with admin override
     */
    public BookingResponseDto completeBooking(Long adminId, Long bookingId) {
        try {
            BookingResponseDto booking = bookingClient.put()
                    .uri("/bookings/{bookingId}/complete", bookingId)
                    .retrieve()
                    .body(BookingResponseDto.class);

            // Log the admin action
            String adminUsername = getUsernameById(adminId);
            AuditLog auditLog = AuditLog.builder()
                    .adminUsername(adminUsername)
                    .action("COMPLETE_BOOKING")
                    .targetEntity("Booking")
                    .targetId(bookingId)
                    .details("Booking completed by admin")
                    .build();
            auditLogRepository.save(auditLog);

            return booking;
        } catch (Exception e) {
            log.error("Error completing booking: {}", bookingId, e);
            throw new RuntimeException("Failed to complete booking: " + e.getMessage());
        }
    }

    /**
     * Get booking statistics for admin dashboard
     */
    public Map<String, Object> getBookingStatistics() {
        try {
            // Get all bookings to calculate statistics
            List<BookingResponseDto> allBookings = getAllBookings();
            
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalBookings", allBookings.size());
            stats.put("activeBookings", allBookings.stream()
                    .filter(b-> b.getStatus().toString().equals("BOOKED"))
                    .count());
            stats.put("completedBookings", allBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus().toString()))
                    .count());
            stats.put("cancelledBookings", allBookings.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus().toString()))
                    .count());
            
            return stats;
        } catch (Exception e) {
            log.error("Error calculating booking statistics", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Get bookings in date range
     */
    public List<BookingResponseDto> getBookingsInDateRange(String startDate, String endDate) {
        try {
            BookingResponseDto[] bookings = bookingClient.get()
                    .uri("/bookings/date-range?startDate={startDate}&endDate={endDate}", startDate, endDate)
                    .retrieve()
                    .body(BookingResponseDto[].class);
            return bookings != null ? Arrays.asList(bookings) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching bookings in date range: {} to {}", startDate, endDate, e);
            return Collections.emptyList();
        }
    }
}
