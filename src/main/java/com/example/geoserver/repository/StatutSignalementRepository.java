package com.example.geoserver.repository;

import com.example.geoserver.entity.StatutSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutSignalementRepository extends JpaRepository<StatutSignalement, Long> {
}