package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.entity.Test;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Test,Long> {

}