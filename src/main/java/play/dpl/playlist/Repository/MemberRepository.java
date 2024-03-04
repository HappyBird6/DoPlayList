package play.dpl.playlist.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import play.dpl.playlist.Entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member,String>{
    
}
