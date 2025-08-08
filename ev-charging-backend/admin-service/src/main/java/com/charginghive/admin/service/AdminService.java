package com.charginghive.admin.service;

import com.charginghive.admin.customException.UserNotFoundException;
import com.charginghive.admin.dto.*;
import com.charginghive.admin.model.AuditLog;
import com.charginghive.admin.repository.AuditLogRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.util.List;

@Service
public class AdminService {

    private final RestClient userClient;
    private final RestClient stationClient;
//    private final RestClient authClient;
    private final AuditLogRepository auditLogRepository;


    public AdminService(RestClient.Builder restClientBuilder,AuditLogRepository auditLogRepository) {
        // Use the service name registered with Eureka, prefixed with "lb://"
        this.userClient = restClientBuilder.baseUrl("lb://AUTH-SERVICE").build();
        this.stationClient = restClientBuilder.baseUrl("lb://STATION-SERVICE").build();
        this.auditLogRepository = auditLogRepository;
    }

    private String getUsernameById(Long adminId) {

        ResponseEntity<AdminDto> responseEntity = userClient.get()
                .uri("/auth/get-by-id/{id}", adminId)
                .retrieve()
                .toEntity(AdminDto.class);

        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
        }

        AdminDto user = responseEntity.getBody();

        if (user != null) {
            String username = user.getName();
            if (username != null) {
                return username;
            } else {
                throw new UserNotFoundException("Username not found for admin ID: " + adminId);
            }
        } else {
            throw new UserNotFoundException("Admin user with ID " + adminId + " not found.");
        }
    }

// --- station

    @Transactional
    public void approveOrRejectStation(Long userId, StationApprovalDto approvalDto) {
        //Call the Station Service to update the station
        System.out.println(userId);
        stationClient.put()
                .uri("/api/stations/update-status") // Assuming this is the endpoint in Station Service
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
                .uri("/api/stations")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }

    public List<StationDto> getUnapprovedStations() {
        return stationClient.get()
                .uri("/api/stations/unapproved")
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

    // --- Admin Service ---

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}
