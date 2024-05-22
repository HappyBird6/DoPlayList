package play.dpl.playlist.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.PlaylistRequestHistory;

@Repository
public interface PlaylistRequestHistoryRepository extends JpaRepository<PlaylistRequestHistory,Long>{
    
    @Transactional
    @Query(value = "SELECT MAX(prh.id) FROM PlaylistRequestHistory prh")
    Optional<Long> findMaxId();

    @Transactional
    @Query(value="SELECT DISTINCT prh.playlistId, p.ytTitle FROM PlaylistRequestHistory prh INNER JOIN Playlist p ON prh.playlistId = p.id WHERE prh.memberId = :memberId")
    List<Object[]> findDistinctPlaylistIdAndYtTitleByMemberId(String memberId);
}
