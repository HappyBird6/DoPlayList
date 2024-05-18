package play.dpl.playlist.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// @Table(schema="tester1")
public class Playlist {
    
    @Id
    private String id;
    private String ytTitle;
    private String ytChannel;
    private String ytPostDate;
    private String ytMusicTitlesJson;
    private Long requestCount;
}