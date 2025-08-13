package com.example.service;

import com.example.entity.Test;
import com.example.repository.TestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TestService{

    private final TestRepository testRepository;


    public List<Test> getAllRecords() {
        return testRepository.findAll();
    }

    public String saveTestEntity(Test test) {
        testRepository.save(test);
        return "Object saved successfully!";
    }
}