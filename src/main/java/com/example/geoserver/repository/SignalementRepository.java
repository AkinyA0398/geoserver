package com.example.geoserver.repository;

import com.example.geoserver.entity.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
        SELECT AVG(
            EXTRACT(EPOCH FROM (st2.date_statut - st1.date_statut)) / 86400
        )
        FROM statut_signalement st1
        JOIN statut_signalement st2
            ON st1.id_signalement = st2.id_signalement
        WHERE st1.id_statut = :fromStatut
          AND st2.id_statut = :toStatut
          AND st2.date_statut > st1.date_statut
        """, nativeQuery = true)
    Double averageDelayBetweenStatuts(
            @Param("fromStatut") Long fromStatut,
            @Param("toStatut") Long toStatut
    );
    
    List<Signalement> findBySyncFalse();
}
