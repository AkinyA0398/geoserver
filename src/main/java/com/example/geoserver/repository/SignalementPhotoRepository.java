package com.example.geoserver.repository;

import com.example.geoserver.entity.SignalementPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SignalementPhotoRepository extends JpaRepository<SignalementPhoto, Long> {
    List<SignalementPhoto> findBySignalementId(Long signalementId);
}
