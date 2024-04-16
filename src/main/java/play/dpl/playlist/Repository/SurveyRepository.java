package play.dpl.playlist.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Entity.Survey;

@Repository
public interface SurveyRepository extends JpaRepository<Survey,Long>{
    
    @Transactional
    @Query(value = "SELECT MAX(s.id) FROM Survey s")
    Optional<Long> findMaxId();
}
