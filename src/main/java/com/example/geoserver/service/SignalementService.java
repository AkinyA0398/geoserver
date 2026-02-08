package com.example.geoserver.service;

import com.example.geoserver.config.FirebaseInitializer;
import com.example.geoserver.entity.Signalement;
import com.example.geoserver.entity.Statut;
import com.example.geoserver.entity.StatutSignalement;
import com.example.geoserver.repository.SignalementRepository;
import com.example.geoserver.repository.StatutRepository;
import com.example.geoserver.repository.StatutSignalementRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private StatutRepository statutRepository;

    @Autowired
    private StatutSignalementRepository statutSignalementRepository;

    @PostConstruct
    public void init() throws Exception {
        FirebaseInitializer.getFirebaseApp();
    }

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

    public void changerStatut(Long signalementId, Long nouveauStatutId) throws Exception {

        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new Exception("Signalement introuvable"));

        Statut nouveauStatut = statutRepository.findById(nouveauStatutId)
                .orElseThrow(() -> new Exception("Statut introuvable"));

        StatutSignalement statutActuel = signalement.getStatutActuel();

        if (statutActuel != null) {
            int ordreActuel = statutActuel.getStatut().getOrdre();
            int nouvelOrdre = nouveauStatut.getOrdre();
            if (nouvelOrdre <= ordreActuel) {
                throw new Exception(
                        "Transition de statut invalide"
                );
            }
        }

        StatutSignalement ss = new StatutSignalement();
        ss.setSignalement(signalement);
        ss.setStatut(nouveauStatut);
        ss.setDateStatut(LocalDateTime.now());

        statutSignalementRepository.save(ss);
    }

    public String creerSignalement(Map<String, Object> payload) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        String signalementId = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("description", payload.get("description"));
        data.put("surface", payload.get("surface"));
        data.put("budget", payload.get("budget"));
        data.put("dateCreation", Instant.now());

        Map<String, Object> geo = new HashMap<>();
        geo.put("latitude", payload.get("latitude"));
        geo.put("longitude", payload.get("longitude"));
        data.put("geom", geo);

         data.put("utilisateurId", payload.get("utilisateurId"));
        data.put("entrepriseId", payload.get("entrepriseId"));
        data.put("typeSignalementId", payload.get("typeSignalementId"));

        data.put("sync", false);

        db.collection("signalements")
                .document(signalementId)
                .set(data)
                .get();

        return signalementId;
    }

    public void syncSignalements() throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        List<QueryDocumentSnapshot> firestoreDocs =
                db.collection("signalements").get().get().getDocuments();

        for (QueryDocumentSnapshot fsDoc : firestoreDocs) {

            Long postgresId = fsDoc.contains("postgresId")
                    ? fsDoc.getLong("postgresId")
                    : null;

            Boolean fsSync = fsDoc.getBoolean("sync");

            if (postgresId == null) {
                createPostgresFromFirestore(fsDoc);
                continue;
            }

            Optional<Signalement> optPg = signalementRepository.findById(postgresId);

            if (optPg.isEmpty()) {
                createPostgresFromFirestore(fsDoc);
                continue;
            }

            Signalement pg = optPg.get();
            Boolean pgSync = pg.getSync();

            if (Boolean.FALSE.equals(pgSync) && Boolean.FALSE.equals(fsSync)) {
                updateFirestoreFromPostgres(pg, fsDoc.getId());
            }
        }

        signalementRepository.findBySyncFalse().forEach(s -> {
            s.setSync(true);
            signalementRepository.save(s);
        });
    }

    private void createPostgresFromFirestore(QueryDocumentSnapshot fsDoc) {

        Signalement s = new Signalement();
        s.setDescription(fsDoc.getString("description"));
        s.setSurface(fsDoc.getDouble("surface"));
        s.setBudget(fsDoc.getDouble("budget"));
        s.setDateCreation(
                fsDoc.getTimestamp("dateCreation").toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
        );
        s.setSync(true);

        Signalement saved = signalementRepository.save(s);

        FirestoreClient.getFirestore()
                .collection("signalements")
                .document(fsDoc.getId())
                .update(Map.of(
                        "postgresId", saved.getId(),
                        "sync", true
                ));
    }

    private void updateFirestoreFromPostgres(Signalement pg, String firestoreId) {

        Map<String, Object> data = new HashMap<>();
        data.put("description", pg.getDescription());
        data.put("surface", pg.getSurface());
        data.put("budget", pg.getBudget());
        data.put("dateCreation", pg.getDateCreation());
        data.put("sync", true);

        FirestoreClient.getFirestore()
                .collection("signalements")
                .document(firestoreId)
                .update(data);
    }
}