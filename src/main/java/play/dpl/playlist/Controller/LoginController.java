package play.dpl.playlist.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Service.CookieService;
import play.dpl.playlist.Service.LoginService;
import play.dpl.playlist.Service.MemberService;
import play.dpl.playlist.Service.YoutubeService;

@Controller
@RequestMapping(value = "/login/oauth2", produces = "application/json")
public class LoginController {

    @Autowired
    LoginService loginService;
    @Autowired
    YoutubeService youtubeService;
    @Autowired
    MemberService memberService;
    @Autowired
    CookieService cookieService;
    @GetMapping("/code/google")
    public void googleLogin(@RequestParam String code,HttpServletResponse response) throws IOException {
        try{
            Member member =  loginService.googleLogin(code); //accessCode, email 받아옴
            List<String[]> list = youtubeService.getPlayList(member.getAccessCode(),null);
            String playlistList =""; 
            for(var e : list){
                String temp = e[1]+"#"+e[0]+",";
                playlistList += temp;                
            }
            if(!playlistList.equals("")) playlistList.substring(0,playlistList.length()-1);
            member.setPlaylistList(playlistList);
            
            if(memberService.saveMember(member)){
                Cookie jwt = cookieService.generateJwtToken(member);
                response.addCookie(jwt);
            }
            response.sendRedirect("/");
        }catch(Exception e){
            e.printStackTrace();
            response.sendRedirect("/error");
        }
    }
}
