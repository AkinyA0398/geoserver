package com.example.geoserver.repository;

import com.example.geoserver.entity.TypeSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeSignalementRepository extends JpaRepository<TypeSignalement, Long> {
}