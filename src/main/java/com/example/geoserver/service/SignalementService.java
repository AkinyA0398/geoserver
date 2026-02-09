package com.example.geoserver.service;

import com.example.geoserver.config.FirebaseInitializer;
import com.example.geoserver.entity.Signalement;
import com.example.geoserver.entity.SignalementPhoto;
import com.example.geoserver.entity.Statut;
import com.example.geoserver.entity.StatutSignalement;
import com.example.geoserver.repository.SignalementRepository;
import com.example.geoserver.repository.StatutRepository;
import com.example.geoserver.repository.StatutSignalementRepository;
import com.example.geoserver.repository.UtilisateurRepository;
import com.example.geoserver.util.ImageCompressor;
import com.example.geoserver.repository.EntrepriseRepository;
import com.example.geoserver.repository.SignalementPhotoRepository;
import com.example.geoserver.repository.TypeSignalementRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private TypeSignalementRepository typeSignalementRepository;

    @Autowired
    private SignalementPhotoRepository signalementPhotoRepository;

    @PostConstruct
    public void init() throws Exception {
        FirebaseInitializer.getFirebaseApp();
    }

    private static final String COLLECTION = "signalements";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    public List<Map<String, Object>> listSignalements(String idUtilisateur) throws Exception {

        Query query = db().collection(COLLECTION);

        if (idUtilisateur != null && !idUtilisateur.isBlank()) {
            query = query.whereEqualTo("utilisateur.id", idUtilisateur);
        }

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();

        List<Map<String, Object>> results = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            results.add(data);
        }

        return results;
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

            double taux = statutActuel.getStatut() != null ? statutActuel.getStatut().getAvancement() / 100.0 : 0;

            if (s.getSurface() != null)
                surfaceReparee += s.getSurface() * taux;

            String nomStatut = statutActuel.getStatut() != null ? statutActuel.getStatut().getNom() : "Inconnu";
            repartitionParStatut.merge(nomStatut, 1, Integer::sum);
        }

        double avancementGlobal = surfaceTotale == 0 ? 0 : (surfaceReparee / surfaceTotale) * 100;

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
                throw new Exception("Transition de statut invalide");
            }
        }

        StatutSignalement ss = new StatutSignalement();
        ss.setSignalement(signalement);
        ss.setStatut(nouveauStatut);
        ss.setDateStatut(LocalDateTime.now());

        statutSignalementRepository.save(ss);

        signalement.setSync(false);
        signalementRepository.save(signalement);
    }

    @SuppressWarnings("unchecked")
    public String creerSignalement(Map<String, Object> payload, List<byte[]> photos) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        String signalementId = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();
        data.put("description", payload.get("description"));
        data.put("surface", payload.get("surface"));
        data.put("budget", payload.get("budget"));
        data.put("dateCreation", Instant.now());

        Map<String, Object> geom = new HashMap<>();
        geom.put("latitude", payload.get("latitude"));
        geom.put("longitude", payload.get("longitude"));
        data.put("geom", geom);

        Map<String, Object> utilisateur = (Map<String, Object>) payload.get("utilisateur");
        Map<String, Object> entreprise = (Map<String, Object>) payload.get("entreprise");
        Map<String, Object> typeSignalement = (Map<String, Object>) payload.get("typeSignalement");

        data.put("utilisateur", utilisateur);
        data.put("entreprise", entreprise);
        data.put("typeSignalement", typeSignalement);

        Map<String, Object> statutActuel = new HashMap<>();
        statutActuel.put("id", 2L);
        statutActuel.put("nom", "Nouveau");
        statutActuel.put("ordre", 1);
        statutActuel.put("avancement", 0);
        statutActuel.put("dateStatut", Instant.now());
        data.put("statutActuel", statutActuel);

        List<String> photosBase64 = new ArrayList<>();
        if (photos != null) {
            for (byte[] photo : photos) {
                byte[] resized = ImageCompressor.resizeAndCompressImage(photo);
                photosBase64.add(Base64.getEncoder().encodeToString(resized));
            }
        }
        data.put("photos", photosBase64);

        data.put("sync", false);

        db.collection("signalements")
                .document(signalementId)
                .set(data)
                .get();

        return signalementId;
    }

    public Map<String, Object> syncSignalements() throws Exception {
        int firebaseToPostgres = 0, postgresToFirebase = 0;
        Firestore db = FirestoreClient.getFirestore();

        List<QueryDocumentSnapshot> firestoreDocs = db.collection("signalements").get().get().getDocuments();

        for (QueryDocumentSnapshot fsDoc : firestoreDocs) {

            Long postgresId = fsDoc.contains("postgresId")
                    ? fsDoc.getLong("postgresId")
                    : null;

            Boolean fsSync = fsDoc.getBoolean("sync");

            if (postgresId == null) {
                firebaseToPostgres++;
                createPostgresFromFirestore(fsDoc);
                continue;
            }

            Optional<Signalement> optPg = signalementRepository.findById(postgresId);

            if (optPg.isEmpty()) {
                firebaseToPostgres++;
                createPostgresFromFirestore(fsDoc);
                continue;
            }

            Signalement pg = optPg.get();
            Boolean pgSync = pg.getSync();

            if (Boolean.FALSE.equals(pgSync) && Boolean.FALSE.equals(fsSync)) {
                postgresToFirebase++;
                if (fsDoc.getLong("statutActuel.id") != null && pg.getStatutActuel() != null
                        && !fsDoc.getLong("statutActuel.id").equals(pg.getStatutActuel().getStatut().getId())) {
                    sendNotification(pg.getUtilisateur() != null ? pg.getUtilisateur().getEmail() : null);
                }
                updateFirestoreFromPostgres(pg, fsDoc.getId());
            }
        }

        signalementRepository.findBySyncFalse().forEach(s -> {
            s.setSync(true);
            signalementRepository.save(s);
        });
        return Map.of(
                "firebaseToPostgres", firebaseToPostgres,
                "postgresToFirebase", postgresToFirebase
        );
    }

    private void sendNotification(String email) {
        try {
            if (email == null)
                return;

            Firestore db = FirestoreClient.getFirestore();

            QuerySnapshot snap = db.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();

            if (snap.isEmpty())
                return;

            String token = snap.getDocuments().get(0).getString("fcmToken");
            if (token == null)
                return;

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Statut du signalement mis à jour")
                            .setBody("Le statut de votre signalement a été modifié")
                            .build())
                    .putData("type", "SIGNALMENT_STATUS")
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void createPostgresFromFirestore(QueryDocumentSnapshot fsDoc) throws Exception {
        Signalement s = new Signalement();
        s.setDescription(fsDoc.getString("description"));
        s.setSurface(fsDoc.getDouble("surface"));
        s.setBudget(fsDoc.getDouble("budget"));
        s.setDateCreation(
                fsDoc.getTimestamp("dateCreation").toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime());

        Map<String, Object> utilisateurMap = (Map<String, Object>) fsDoc.get("utilisateur");
        if (utilisateurMap != null && utilisateurMap.get("id") != null) {
            String utilisateurId = utilisateurMap.get("id").toString();
            s.setUtilisateur(
                    utilisateurRepository.findById(utilisateurId)
                            .orElseThrow(() -> new Exception("Utilisateur introuvable avec ID: " + utilisateurId)));
        }

        Map<String, Object> entrepriseMap = (Map<String, Object>) fsDoc.get("entreprise");
        if (entrepriseMap != null && entrepriseMap.get("id") != null) {
            Long entrepriseId = ((Number) entrepriseMap.get("id")).longValue();
            s.setEntreprise(
                    entrepriseRepository.findById(entrepriseId)
                            .orElseThrow(() -> new Exception("Entreprise introuvable avec ID: " + entrepriseId)));
        }

        Map<String, Object> typeMap = (Map<String, Object>) fsDoc.get("typeSignalement");
        if (typeMap != null && typeMap.get("id") != null) {
            Long typeId = ((Number) typeMap.get("id")).longValue();
            s.setTypeSignalement(
                    typeSignalementRepository.findById(typeId)
                            .orElseThrow(() -> new Exception("TypeSignalement introuvable avec ID: " + typeId)));
        }

        s.setSync(true);

        Signalement saved = signalementRepository.save(s);

        Map<String, Object> statutMap = (Map<String, Object>) fsDoc.get("statutActuel");
        if (statutMap != null && statutMap.get("id") != null) {
            Long statutId = ((Number) statutMap.get("id")).longValue();
            Statut statut = statutRepository.findById(statutId)
                    .orElseThrow(() -> new Exception("Statut introuvable avec ID: " + statutId));

            StatutSignalement ss = new StatutSignalement();
            ss.setSignalement(saved);
            ss.setStatut(statut);

            Instant statutInstant = (Instant) statutMap.get("dateStatut");
            ss.setDateStatut(LocalDateTime.ofInstant(statutInstant, java.time.ZoneId.systemDefault()));

            statutSignalementRepository.save(ss);
        }

        List<String> photosBase64 = (List<String>) fsDoc.get("photos");
        if (photosBase64 != null) {
            for (String b64 : photosBase64) {
                SignalementPhoto sp = new SignalementPhoto();
                sp.setSignalement(saved);
                sp.setPhoto(Base64.getDecoder().decode(b64));
                signalementPhotoRepository.save(sp);
            }
        }

        FirestoreClient.getFirestore()
                .collection("signalements")
                .document(fsDoc.getId())
                .update(Map.of(
                        "postgresId", saved.getId(),
                        "sync", true));
    }

    private void updateFirestoreFromPostgres(Signalement pg, String firestoreId) {

        Map<String, Object> data = new HashMap<>();
        data.put("description", pg.getDescription());
        data.put("surface", pg.getSurface());
        data.put("budget", pg.getBudget());
        data.put("dateCreation", pg.getDateCreation());
        data.put("sync", true);

        StatutSignalement statutActuel = pg.getStatutActuel();
        if (statutActuel != null) {
            Map<String, Object> statutMap = new HashMap<>();
            statutMap.put("id", statutActuel.getStatut().getId());
            statutMap.put("nom", statutActuel.getStatut().getNom());
            statutMap.put("ordre", statutActuel.getStatut().getOrdre());
            statutMap.put("avancement",
                    statutActuel.getStatut().getAvancement());
            statutMap.put("dateStatut",
                    statutActuel.getDateStatut());

            data.put("statutActuel", statutMap);
        }

        FirestoreClient.getFirestore()
                .collection("signalements")
                .document(firestoreId)
                .update(data);
    }
}