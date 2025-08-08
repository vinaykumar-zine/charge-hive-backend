package com.example.controller;


import com.example.entity.Test;
import com.example.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/route")
@AllArgsConstructor
class TestController{

    private final TestService testService;

    @GetMapping
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok().body(testService.getAllRecords());
    }

    @PostMapping
    public ResponseEntity<?> postOne(@RequestBody Test test){
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.saveTestEntity(test));
    }
}
