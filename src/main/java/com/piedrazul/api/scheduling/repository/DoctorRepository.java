package com.piedrazul.api.scheduling.repository;

import java.util.List;
import java.util.Optional;

import com.piedrazul.api.scheduling.domain.Doctor;

/**
 * DoctorRepository
 */
public interface DoctorRepository {

    Optional<Doctor> findById(String id);
    List<Doctor> findAll();
}