package com.example.geoserver.controller;

import com.example.geoserver.dto.ApiResponse;
import com.example.geoserver.dto.SignalementDTO;
import com.example.geoserver.entity.Signalement;
import com.example.geoserver.service.SignalementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() { 
        try {
            Object stats = signalementService.getStats();
            return ResponseEntity.ok(new ApiResponse<>(true, stats, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/avalider")
    public ResponseEntity<ApiResponse<List<SignalementDTO>>> getAllAValider() {
        try {
            List<Signalement> list = signalementService.getAllAValider();
            List<SignalementDTO> listDTO = list.stream()
                    .map(SignalementDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, listDTO, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/valide")
    public ResponseEntity<ApiResponse<List<SignalementDTO>>> getAllValide() {   
        try {
            List<Signalement> list = signalementService.getAllValide();
            List<SignalementDTO> listDTO = list.stream()
                    .map(SignalementDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, listDTO, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/refuse")
    public ResponseEntity<ApiResponse<List<SignalementDTO>>> getAllRefuse() {
        try {
            List<Signalement> list = signalementService.getAllRefuse();
            List<SignalementDTO> listDTO = list.stream()
                    .map(SignalementDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, listDTO, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @GetMapping("/stats/average-delay")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getAverageDelay() {
        try {
            Map<String, Double> delays = new HashMap<>();
            delays.put("nouveauVersEnCours", signalementService.moyenneNouveauVersEnCours());
            delays.put("enCoursVersTermine", signalementService.moyenneEnCoursVersTermine());
            delays.put("nouveauVersTermine", signalementService.moyenneNouveauVersTermine());
            return ResponseEntity.ok(new ApiResponse<>(true, delays, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<ApiResponse<String>> changerStatut(
            @PathVariable Long id,
            @RequestParam Long nouveauStatutId) {
        try {
            signalementService.changerStatut(id, nouveauStatutId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Statut changé avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> creerSignalement(@RequestBody Map<String, Object> payload) {
        try {
            String signalementId = signalementService.creerSignalement(payload);
            return ResponseEntity.ok(new ApiResponse<>(true, signalementId, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}