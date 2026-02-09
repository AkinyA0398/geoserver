package com.example.geoserver.controller;

import com.example.geoserver.dto.ApiResponse;
import com.example.geoserver.dto.SignalementDTO;
import com.example.geoserver.entity.Signalement;
import com.example.geoserver.service.SignalementService;
import com.example.geoserver.util.ImageCompressor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private ObjectMapper objectMapper;

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

    @GetMapping("/firebase")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String idUtilisateur) {
        try {
            return ResponseEntity.ok(signalementService.listSignalements(idUtilisateur));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
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

    @SuppressWarnings("unchecked")
    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> creerSignalement(
            @RequestPart("payload") String payloadJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        try {
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

            String signalementId = signalementService.creerSignalement(
                    payload,
                    ImageCompressor.multipartFilesToBytes(photos));

            return ResponseEntity.ok(new ApiResponse<>(true, signalementId, null));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncSignalements() {
        try {
            Map<String, Object> result = signalementService.syncSignalements();
            return ResponseEntity.ok(new ApiResponse<>(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}