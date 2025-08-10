package com.charginghive.admin.controller;

import com.charginghive.admin.dto.*;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    /*
     * This must be an instance field for Spring's Dependency Injection.
     *
     * It is not 'static' because that would break the Inversion of Control (IoC) pattern
     */
    private final AdminService adminService;

    // Station Endpoints
    @PostMapping("/stations/process-approval")
    public ResponseEntity<Void> processStationApproval(@RequestHeader("X-User-Id") Long adminId, @RequestBody StationApprovalDto approvalDto) {
        log.info("Received request to process station approval from adminId: {}", adminId);
        log.debug("Approval DTO details: {}", approvalDto);
        adminService.approveOrRejectStation(adminId, approvalDto);
        log.info("Successfully processed station approval for stationId: {}", approvalDto.getStationId());
        return ResponseEntity.ok().build();
    }

//    // Station Endpoints
//    @PostMapping("/stations/process-approval")
//    public ResponseEntity<Void> processStationApproval(@RequestBody StationApprovalDto approvalDto) {
//        adminService.approveOrRejectStation(getCurrentAdminUsername(), approvalDto);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/stations")
    public ResponseEntity<List<StationDto>> getAllStations() {
        log.info("Received request to get all stations.");
        List<StationDto> stations = adminService.getAllStations();
        log.info("Found {} stations.", stations.size());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/stations/unapproved")
    public ResponseEntity<List<StationDto>> getUnapprovedStations() {
        log.info("Received request to get all unapproved stations.");
        List<StationDto> unapprovedStations = adminService.getUnapprovedStations();
        log.info("Found {} unapproved stations.", unapprovedStations.size());
        return ResponseEntity.ok(unapprovedStations);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Received request to get all users.");
        List<UserDto> users = adminService.getAllUsers();
        log.info("Found {} users.", users.size());
        return ResponseEntity.ok(users);
    }

    //must return user with recent 5-10 bookings
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailDto> getUserWithBooking(@PathVariable("userId") Long userId) {
        log.info("Received request to get all users.");
        UserDetailDto user = adminService.getUserDetials(userId);
        return ResponseEntity.ok(user);
    }


    //endpoints to be implemented later
    //get user with all bookings with pagination support
    //get user with all payments with pagination support
    //get all bookings with pagination support
    //get all payments with pagination support


    // Audit Log Endpoint
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        log.info("Received request to get audit logs.");
        List<AuditLog> auditLogs = adminService.getAuditLogs();
        log.info("Found {} audit log entries.", auditLogs.size());
        return ResponseEntity.ok(auditLogs);
    }

    // --- Booking Management Endpoints ---

    /**
     * Get all bookings for admin overview
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        log.info("Received request to get all bookings.");
        List<BookingResponseDto> bookings = adminService.getAllBookings();
        log.info("Found {} bookings.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get bookings by status for admin filtering
     */
    @GetMapping("/bookings/status/{status}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByStatus(@PathVariable String status) {
        log.info("Received request to get bookings by status: {}", status);
        List<BookingResponseDto> bookings = adminService.getBookingsByStatus(status);
        log.info("Found {} bookings with status: {}", bookings.size(), status);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get active bookings for admin monitoring
     */
    @GetMapping("/bookings/active")
    public ResponseEntity<List<BookingResponseDto>> getActiveBookings() {
        log.info("Received request to get active bookings.");
        List<BookingResponseDto> bookings = adminService.getActiveBookings();
        log.info("Found {} active bookings.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking - admin override capability
     */
    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@RequestHeader("X-User-Id") Long adminId, 
                                                           @PathVariable Long bookingId,
                                                           @RequestParam(required = false) String reason) {
        log.info("Admin {} cancelling booking {}", adminId, bookingId);
        BookingResponseDto booking = adminService.cancelBooking(adminId, bookingId, reason);
        return ResponseEntity.ok(booking);
    }

    /**
     * Complete a booking - admin override capability
     */
    @PutMapping("/bookings/{bookingId}/complete")
    public ResponseEntity<BookingResponseDto> completeBooking(@RequestHeader("X-User-Id") Long adminId, 
                                                             @PathVariable Long bookingId) {
        log.info("Admin {} completing booking {}", adminId, bookingId);
        BookingResponseDto booking = adminService.completeBooking(adminId, bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Get booking statistics for dashboard
     */
    @GetMapping("/bookings/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        log.info("Received request for booking statistics.");
        Map<String, Object> statistics = adminService.getBookingStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get bookings in date range for reporting
     */
    @GetMapping("/bookings/date-range")
    public ResponseEntity<List<BookingResponseDto>> getBookingsInDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Getting bookings from {} to {}", startDate, endDate);
        List<BookingResponseDto> bookings = adminService.getBookingsInDateRange(startDate, endDate);
        log.info("Found {} bookings in date range.", bookings.size());
        return ResponseEntity.ok(bookings);
    }

}
