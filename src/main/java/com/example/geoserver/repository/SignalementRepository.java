package com.example.geoserver.repository;

import com.example.geoserver.entity.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    @Query("SELECT s FROM Signalement s " +
           "JOIN s.statuts st " +
           "WHERE st.dateStatut = (" +
           "   SELECT MAX(st2.dateStatut) FROM StatutSignalement st2 WHERE st2.signalement = s" +
           ") AND st.statut.ordre > 20")
    List<Signalement> findAllValide();

    @Query("SELECT s FROM Signalement s " +
           "JOIN s.statuts st " +
           "WHERE st.dateStatut = (" +
           "   SELECT MAX(st2.dateStatut) FROM StatutSignalement st2 WHERE st2.signalement = s" +
           ") AND st.statut.ordre = 10")
    List<Signalement> findAllAValider();

    @Query("SELECT s FROM Signalement s " +
           "JOIN s.statuts st " +
           "WHERE st.dateStatut = (" +
           "   SELECT MAX(st2.dateStatut) FROM StatutSignalement st2 WHERE st2.signalement = s" +
           ") AND st.statut.id = 20")
    List<Signalement> findAllRefuse();   
}
