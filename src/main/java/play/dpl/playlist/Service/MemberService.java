package play.dpl.playlist.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Repository.MemberRepositoryImpl;
import play.dpl.playlist.Repository.PlaylistRequestHistoryRepository;


@Service
public class MemberService implements UserDetailsService {
    @Autowired
    private MemberRepositoryImpl repository;
    @Autowired
    private PlaylistRequestHistoryRepository playlistRequestHistoryRepository;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Member> member = repository.findById(email);

        // Converting userDetail to UserDetails
        return member.map(MemberInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
    }
    public int saveMember(Member member){
        // 이미 있는 유저 : 1
        // 신규 유저 : 0
        // 실패 : 2
        try{
            if(repository.findById(member.getEmail()).isPresent()){
                repository.updateMember(member);
                return 1;
            }else{
                return 0;
            }
        }catch(Exception e){
            System.out.println("MemberService.saveMember ERROR : "+e.getMessage());
            e.printStackTrace();
            return 2;
        }
    }
    public boolean addUser(Member member) {
        try {
            member.setPassword(encoder.encode(member.getEmail()+new Date().getTime()));
            
            repository.save(member);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getHistory(String memberId){
        List<Object[]> list = playlistRequestHistoryRepository.findDistinctPlaylistIdAndYtTitleByMemberId(memberId);

        return list;
    }

}
