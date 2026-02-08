package com.example.geoserver.service;

import com.example.geoserver.entity.Signalement;
import com.example.geoserver.repository.SignalementRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    public List<Signalement> getAll() {
        return signalementRepository.findAll();
    }
}