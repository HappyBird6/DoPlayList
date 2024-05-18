package play.dpl.playlist.Entity;

import java.util.Date;

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
public class Member {
    
    @Id
    private String email;
    private String accessCode;
    private String playlistList;
    private String password;
    private Date signupDate;
    private Date signinDate;
    // getters and setters
}