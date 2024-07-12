package play.dpl.playlist.Entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PlaylistRequestHistory {
    
    @Id
    private Long id;
    private String memberId;
    private String playlistId;
    private Date requestAt;
    
}