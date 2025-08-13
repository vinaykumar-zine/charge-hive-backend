package com.charginghive.station.service;

import com.charginghive.station.customException.OwnerIdMissMatchException;
import com.charginghive.station.customException.UserNotFoundException;
import com.charginghive.station.dto.*;
import com.charginghive.station.model.Station;
import com.charginghive.station.model.StationPort;
import com.charginghive.station.repository.StationPortRepository;
import com.charginghive.station.repository.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
//@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationRepository stationRepository;
    private final StationPortRepository stationPortRepository;
    private final ModelMapper modelMapper;
    private final RestClient userClient;
    private final RestClient bookingClient;

    public StationService(StationRepository repository, StationPortRepository repositoryPort, ModelMapper modelMapper, RestClient.Builder userClient, RestClient.Builder bookingClient) {
           this.stationRepository = repository;
           this.stationPortRepository = repositoryPort;
           this.modelMapper = modelMapper;
           this.userClient = userClient
                   .baseUrl("http://AUTH-SERVICE")
                   .build();
           this.bookingClient = bookingClient.baseUrl("http://BOOKING-SERVICE")
                   .build();
    }



    @Transactional
    public StationDto createStation(CreateStationRequestDto requestDto,Long ownerId) {
        //Verify the ownerId exists by calling the User Service
        log.info("ownerId={}",ownerId);
        if(!verifyUserExists(ownerId)) {
            throw new UserNotFoundException("Owner not found with id "+ownerId);
        }
         log.info("Station info {}.", requestDto);
        //Use ModelMapper to map the basic properties
        Station station = modelMapper.map(requestDto, Station.class);
        station.setOwnerId(ownerId);
        station.setApproved(false); // New stations must always unapproved by default.

        //Manually handle the nested list of ports to set the bidirectional relationship
        if (requestDto.getPorts() != null && !requestDto.getPorts().isEmpty()) {
            requestDto.getPorts().forEach(portDto -> {
                StationPort port = modelMapper.map(portDto, StationPort.class);
                port.setStation(station); // Link the port to its parent station
                station.getPorts().add(port);
            });
        }

        Station savedStation = stationRepository.save(station);
        return modelMapper.map(station, StationDto.class);
    }

    public List<StationDto> getAllStations() {
        return stationRepository.findAll().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }

    public StationDto getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + id));
        return modelMapper.map(station, StationDto.class);
    }

    public List<StationDto> getUnapprovedStations() {
        return stationRepository.findByIsApprovedFalse().stream()
                .map(station -> modelMapper.map(station, StationDto.class)).toList();
    }

    public List<StationDto> getStationsByOwner(Long ownerId) {
        return stationRepository.findByOwnerId(ownerId)
                .stream().map(station -> modelMapper.map(station, StationDto.class)).toList();
    }


    @Transactional
    public void updateStationStatus(StationApprovalDto approvalDto) {
        Station station = stationRepository.findById(approvalDto.getStationId())
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + approvalDto.getStationId()));

        station.setApproved(approvalDto.isApproved());
        stationRepository.save(station);
    }

    @Transactional
    public void deleteStation(Long stationId, Long ownerId)throws OwnerIdMissMatchException ,EntityNotFoundException{
            Station station = stationRepository.findById(stationId).orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
            if(!ownerId.equals(station.getOwnerId())){
                throw new OwnerIdMissMatchException("Station does not belong to the owner");
            }
            stationRepository.deleteById(stationId);
    }

    @Transactional
    public void deletePort(Long portId) {
        if (!stationPortRepository.existsById(portId)) {
            throw new EntityNotFoundException("Station Port not found with id: " + portId);
        }
        stationPortRepository.deleteById(portId);
    }

    @Transactional
    public StationDto updateStation(Long ownerId, Long stationId, UpdateStationRequestDto dto) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
        if (!ownerId.equals(station.getOwnerId())) {
            throw new OwnerIdMissMatchException("Station does not belong to the owner");
        }
        station.setName(dto.getName());
        station.setAddress(dto.getAddress());
        station.setCity(dto.getCity());
        station.setState(dto.getState());
        station.setPostalCode(dto.getPostalCode());
        station.setLatitude(dto.getLatitude());
        station.setLongitude(dto.getLongitude());
        station.setPricePerHour(dto.getPricePerHour());
        Station saved = stationRepository.save(station);
        return modelMapper.map(saved, StationDto.class);
    }

    @Transactional
    public StationDto addPort(Long ownerId, Long stationId, CreatePortRequestDto dto) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
        if (!ownerId.equals(station.getOwnerId())) {
            throw new OwnerIdMissMatchException("Station does not belong to the owner");
        }
        StationPort port = modelMapper.map(dto, StationPort.class);
        port.setStation(station);
        station.getPorts().add(port);
        Station saved = stationRepository.save(station);
        return modelMapper.map(saved, StationDto.class);
    }

    @Transactional
    public StationDto updatePort(Long ownerId, Long portId, CreatePortRequestDto dto) {
        StationPort port = stationPortRepository.findById(portId)
                .orElseThrow(() -> new EntityNotFoundException("Station Port not found with id: " + portId));
        Station station = port.getStation();
        if (!ownerId.equals(station.getOwnerId())) {
            throw new OwnerIdMissMatchException("Station does not belong to the owner");
        }
        port.setConnectorType(dto.getConnectorType());
        port.setMaxPowerKw(dto.getMaxPowerKw());
        stationPortRepository.save(port);
        return modelMapper.map(station, StationDto.class);
    }

    public List<StationDto> searchStations(String query, String city, Double minPrice, Double maxPrice, Boolean available) {
        return stationRepository.findAll().stream()
                .filter(s -> query == null || s.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(s -> city == null || s.getCity().equalsIgnoreCase(city))
                .filter(s -> minPrice == null || (s.getPricePerHour() != null && s.getPricePerHour() >= minPrice))
                .filter(s -> maxPrice == null || (s.getPricePerHour() != null && s.getPricePerHour() <= maxPrice))
                // available flag could check ports size > 0 for now
                .filter(s -> available == null || (available && !s.getPorts().isEmpty()) || (!available))
                .map(s -> modelMapper.map(s, StationDto.class))
                .toList();
    }

    public List<StationDto> findNearby(double lat, double lng, double radiusKm) {
        final double earthRadiusKm = 6371.0;
        return stationRepository.findAll().stream()
                .filter(Station::isApproved)
                .map(s -> new Object[]{s, distanceKm(lat, lng, s.getLatitude(), s.getLongitude(), earthRadiusKm)})
                .filter(arr -> (double) arr[1] <= radiusKm)
                .sorted(Comparator.comparingDouble(a -> (double) a[1]))
                .map(arr -> modelMapper.map((Station) arr[0], StationDto.class))
                .collect(Collectors.toList());
    }

    private double distanceKm(double lat1, double lon1, Double lat2, Double lon2, double R) {
        if (lat2 == null || lon2 == null) return Double.MAX_VALUE;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<String> getAvailability(Long stationId, String dateIso) {
        // Placeholder: return every 30 min between 08:00 and 20:00 as available
        LocalDate date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_DATE);
        List<String> slots = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(20, 0);
        while (!t.isAfter(end.minusMinutes(30))) {
            slots.add(date.atTime(t).toString());
            t = t.plusMinutes(30);
        }
        return slots;
    }

      private boolean verifyUserExists(Long ownerId) {

          try {
              UserDto user = userClient.get()
                      .uri("auth/get-by-id/{id}", ownerId)
                      //if any 4xx or 5xx status is received then .retrieve()
                      //throw exception
                      .retrieve()
                      .body(UserDto.class);

              if (user == null) {
                  throw new UserNotFoundException("Received an empty response for admin ID: " + ownerId);
              }
          } catch (WebClientResponseException e) {
              throw new UserNotFoundException("Admin user with ID " + ownerId + " not found.");
          }

        return true;
      }

    // --- Methods for Booking Service Integration ---

    /**
     * Check if a station exists
     */
    public boolean stationExists(Long stationId) {
        return stationRepository.existsById(stationId);
    }

    /**
     * Get specific port information for booking service
     */
    public StationPortDto getPortInfo(Long stationId, Long portId) {
        // First check if station exists
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
        
        // Find the port in this station
        StationPort port = station.getPorts().stream()
                .filter(p -> p.getId().equals(portId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Port not found with id: " + portId + " in station: " + stationId));
        
        return modelMapper.map(port, StationPortDto.class);
    }

    /**
     * Get all ports for a station
     */
    public List<StationPortDto> getStationPorts(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));
        
        return station.getPorts().stream()
                .map(port -> modelMapper.map(port, StationPortDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Get only approved stations
     */
    public List<StationDto> getApprovedStations() {
        return stationRepository.findByIsApprovedTrue().stream()
                .map(station -> modelMapper.map(station, StationDto.class))
                .collect(Collectors.toList());
    }

    public TotalEarningRespDto getTotalEarningsFromAStaion(Long stationId) {
        TotalEarningRespDto totalEarning = null;
        try{
            totalEarning = bookingClient.get().uri("/bookings/earnings/"+stationId)
                    .retrieve()
                    .body(TotalEarningRespDto.class);
        }
        catch (Exception e){
            log.warn("Invalid station id: " + stationId);
        }
        return totalEarning;
    }
}