package play.dpl.playlist.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Playlist;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,String>{
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE playlist SET REQUEST_COUNT = REQUEST_COUNT + 1 WHERE ID=':id'",nativeQuery = true)
    void updateRequestCount(@Param("id") String id);
}
