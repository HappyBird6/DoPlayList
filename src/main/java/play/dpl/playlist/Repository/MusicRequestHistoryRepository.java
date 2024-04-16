package play.dpl.playlist.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.MusicRequestHistory;

@Repository
public interface MusicRequestHistoryRepository extends JpaRepository<MusicRequestHistory,Long>{
    
    @Transactional
    @Query(value = "SELECT MAX(mrh.id) FROM MusicRequestHistory mrh")
    Optional<Long> findMaxId();
}
