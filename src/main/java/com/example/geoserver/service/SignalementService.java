package com.example.geoserver.service;

import com.example.geoserver.entity.Signalement;
import com.example.geoserver.entity.StatutSignalement;
import com.example.geoserver.repository.SignalementRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    public List<Signalement> getAll() {
        return signalementRepository.findAll();
    }

    public Map<String, Object> getStats() {

        List<Signalement> signalements = signalementRepository.findAllValide();

        int nombrePoints = signalements.size();
        double surfaceTotale = 0, budgetTotal = 0, surfaceReparee = 0;

        Map<String, Integer> repartitionParStatut = new HashMap<>();

        for (Signalement s : signalements) {

            if (s.getSurface() != null) surfaceTotale += s.getSurface();
            if (s.getBudget() != null) budgetTotal += s.getBudget();
            StatutSignalement statutActuel = s.getStatutActuel();

            if (statutActuel == null) continue;

            Long statutId = statutActuel.getStatut() != null ? 
                                statutActuel.getStatut().getId() : 1;

            double taux = statutActuel.getStatut() != null ? 
                            statutActuel.getStatut().getAvancement() / 100.0 : 0;

            if (s.getSurface() != null)
                surfaceReparee += s.getSurface() * taux;

            String nomStatut = statutActuel.getStatut() != null ? 
                                statutActuel.getStatut().getNom() : "Inconnu";
            repartitionParStatut.merge(nomStatut, 1, Integer::sum);
        }

        double avancementGlobal = surfaceTotale == 0 ? 0 : 
                                    (surfaceReparee / surfaceTotale) * 100;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("miseAJour", LocalDate.now());
        result.put("nombrePoints", nombrePoints);
        result.put("surfaceTotale", surfaceTotale);
        result.put("budgetTotal", budgetTotal);
        result.put("avancementGlobal", Math.round(avancementGlobal));
        result.put("repartitionParStatut", repartitionParStatut);

        return result;
    }

    public List<Signalement> getAllAValider() {
        return signalementRepository.findAllAValider();
    }

    public List<Signalement> getAllValide() {
        return signalementRepository.findAllValide();
    }

    public List<Signalement> getAllRefuse() {
        return signalementRepository.findAllRefuse();
    }

    public double moyenneNouveauVersEnCours() {
        return signalementRepository.averageDelayBetweenStatuts(2L, 4L);
    }

    public double moyenneEnCoursVersTermine() {
        return signalementRepository.averageDelayBetweenStatuts(4L, 5L);
    }

    public double moyenneNouveauVersTermine() {
        return signalementRepository.averageDelayBetweenStatuts(2L, 5L);
    }
}