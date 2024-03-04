package play.dpl.playlist.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import play.dpl.playlist.Entity.Member;

@Service
public class CookieService {

    @Autowired
    private JwtService jwtService;

    public Cookie generateJwtToken(Member member) {
        Cookie cookie = null;
        try{
            String token = jwtService.generateToken(member.getEmail(), member.getPassword());
            
            cookie = new Cookie("jwtCookie", token);
            cookie.setMaxAge(JwtService.JWT_EXPIRY_TIME);
            cookie.setPath("/");
        }catch(Exception e){
            return null;   
        }

        return cookie;
    }
}
