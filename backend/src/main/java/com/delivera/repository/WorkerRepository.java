package com.delivera.repository;

import com.delivera.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {
}
