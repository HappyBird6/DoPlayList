package play.dpl.playlist.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Music;

@Repository
public interface MusicRespository extends JpaRepository<Music,String>{
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE Music SET REQUEST_COUNT = REQUEST_COUNT + 1 WHERE ID=':id'",nativeQuery = true)
    void updateRequestCount(@Param("id") String id);
}
