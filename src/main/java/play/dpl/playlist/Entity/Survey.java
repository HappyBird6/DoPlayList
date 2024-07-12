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
public class Survey {
    
    @Id
    private Long id;
    private String memberId;
    private Integer type;
    private String detail; // Y#Y#Y#디테일
    private Date surveyAt;
}
