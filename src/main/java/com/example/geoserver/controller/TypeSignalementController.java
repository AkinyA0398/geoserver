package com.example.geoserver.controller;

import com.example.geoserver.dto.ApiResponse;
import com.example.geoserver.entity.TypeSignalement;
import com.example.geoserver.dto.TypeSignalementDTO;
import com.example.geoserver.service.TypeSignalementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/types-signalement")
@CrossOrigin(origins = "*")
public class TypeSignalementController {

    @Autowired
    private TypeSignalementService typeSignalementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TypeSignalementDTO>>> getAllTypes() {
        try {
            List<TypeSignalement> types = typeSignalementService.getAllTypes();
            List<TypeSignalementDTO> typesDTO = types.stream()
                .map(TypeSignalementDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, typesDTO, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ApiResponse<>(false, null, e.getMessage()));
        }
    }
}