package play.dpl.playlist.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Mananger.CacheManager;
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
        
            
            int flag = memberService.saveMember(member);
            switch (flag) {
                case 0:
                    // 신규 유저
                    CacheManager.getInstance().getCacheMember().put(member.getEmail(),member);
                    response.sendRedirect("/login/oauth2/signup?e="+member.getEmail());
                    break;
                case 1:
                    // 이미 있는 유저
                    Cookie jwt = cookieService.generateJwtToken(member);
                    response.addCookie(jwt);
                    response.sendRedirect("/");
                    break;
                case 2:
                    throw new Error("멤버 저장 실패");
            }
        }catch(Exception e){
            e.printStackTrace();
            response.sendRedirect("/error");
        }
    }
    @GetMapping("/signup")
    public ModelAndView signup(@RequestParam String e,HttpServletResponse response) {

        ModelAndView mav = new ModelAndView("signup");
        mav.addObject("email", e);
        return mav;
    }
    @PostMapping("/signupOk")
    @ResponseBody
    public int signupOk(@RequestBody String email, HttpServletResponse response) throws IOException {

        System.out.println("이메일 : "+email);
        Member member = CacheManager.getInstance().getCacheMember().getIfPresent(email);
        if(member==null) {
            return 1;
        }else{
            memberService.addUser(member);
            Cookie jwt = cookieService.generateJwtToken(member);
            response.addCookie(jwt);
        }
        return 0;
    }
}
