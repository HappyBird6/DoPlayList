package play.dpl.playlist.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class YoutubeDataRepository {
 
    
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private MusicRespository musicRespository;
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private MusicRequestHistoryRepository musicRequestHistoryRepository;
    @Autowired
    private PlaylistRequestHistoryRepository playlistRequestHistoryRepository;

    // @Transactional
    // public int 

}
