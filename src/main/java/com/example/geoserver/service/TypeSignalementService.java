package com.example.geoserver.service;

import com.example.geoserver.entity.TypeSignalement;
import com.example.geoserver.repository.TypeSignalementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TypeSignalementService {
    
    @Autowired
    private TypeSignalementRepository typeSignalementRepository;
    
    public List<TypeSignalement> getAllTypes() {
        return typeSignalementRepository.findAll();
    }
}