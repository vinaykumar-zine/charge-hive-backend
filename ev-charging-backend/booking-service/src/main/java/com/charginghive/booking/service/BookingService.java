package com.charginghive.booking.service;

import com.charginghive.booking.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    //check port availability
}
