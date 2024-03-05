package play.dpl.playlist.Service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Repository.MemberRepositoryImpl;


@Service
public class MemberService implements UserDetailsService {
    @Autowired
    private MemberRepositoryImpl repository;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Member> member = repository.findById(email);

        // Converting userDetail to UserDetails
        return member.map(MemberInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
    }
    public boolean saveMember(Member member){
        try{
            if(repository.findById(member.getEmail()).isPresent()){
                repository.updateMember(member);
            }else{
                if(!addUser(member)) return false;
            }
        }catch(Exception e){
            System.out.println("MemberService.saveMember ERROR : "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean addUser(Member member) {
        try {
            member.setPassword(encoder.encode(member.getEmail()+new Date().getTime()));
            
            repository.save(member);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
