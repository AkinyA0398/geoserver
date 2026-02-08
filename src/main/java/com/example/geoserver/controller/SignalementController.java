package com.example.geoserver.controller;

import com.example.geoserver.dto.ApiResponse;
import com.example.geoserver.dto.SignalementDTO;
import com.example.geoserver.entity.Signalement;
import com.example.geoserver.entity.Statut;
import com.example.geoserver.entity.StatutSignalement;
import com.example.geoserver.repository.SignalementRepository;
import com.example.geoserver.service.SignalementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/signalements")
@CrossOrigin(origins = "http://localhost:5173")
public class SignalementController {

    @Autowired
    private SignalementService signalementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SignalementDTO>>> getAllSignalements() {
        try {
            List<Signalement> list = signalementService.getAll();
            List<SignalementDTO> listDTO = list.stream()
                    .map(SignalementDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, listDTO, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}