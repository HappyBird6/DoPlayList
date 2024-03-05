package play.dpl.playlist.Controller;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Service.MemberInfoDetails;
import play.dpl.playlist.Service.PlayListService;
import play.dpl.playlist.Service.YoutubeService;


@Controller
public class MainController {
    
    @Autowired
    PlayListService playListService;
    @Autowired
    YoutubeService youtubeService;

    @RequestMapping("/")
    public ModelAndView main() {
        ModelAndView mav = new ModelAndView("index");
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();

            System.out.println("ROLE : "+SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }else{
            System.out.println("로그인X");
        }
        return mav;
    }

    @PostMapping("/getPlayList")
    @ResponseBody
    public String getPlayList(@RequestBody String url, HttpServletResponse response){
        System.out.println("받은 URL : "+url);
        System.out.println("getPlayList 실행");
        long s = new Date().getTime();
        
        List<String> titles = playListService.scrapPage(url);
        String commonPrefix = findCommonPrefix(titles,0);
        System.out.println("같은 prefix [: "+commonPrefix+"]");
        if(commonPrefix!=null){
            titles = removeCommonPrefix(titles, commonPrefix);
        }
        //titles = addCommonSuffix(titles);

        // for(var title : titles){
        //     System.out.println(title);
        //     try{
        //         //youtubeService.findVideoId("ya29.a0AfB_byDrhZOx3HqiVI_jz5fh6MXeUZj8wxZPhnWhs-MO2oYbc45vlCOo2oy5685X1uvvfnfUfsez5IZGjYftNjYd2TpxPNSsaIpoU9ZC_s3bK3HSyskmDY7u2u_pMUBuy355BRmT8luyoVsoBOrxYg5JOlXlquh5t0ghaCgYKAd0SAQ4SFQHGX2Mi__CV42MJWqwh-SRvca-AeQ0171",title);

        //     }catch(Exception e){
        //         e.printStackTrace();
        //     }
        //     System.out.println();
        //     System.out.println();
        // }
        long e = new Date().getTime();
        System.out.println("getPlayList 소요시간 : "+(e-s)/1000f + "초");
        return new Gson().toJson(titles);
    }
    
    private String findCommonPrefix(List<String> titles,int depth) {
        if(titles==null) return null;

        String shortest = titles.get(0).substring(depth,depth+1);
        for (String title : titles) {
            if(!title.substring(depth,depth+1).equals(shortest)){ 
                return null;
            }
        }
        String s = findCommonPrefix(titles, depth+1);
        shortest = s==null ? shortest : shortest +s;

        return shortest;
    }
    private static List<String> removeCommonPrefix(List<String> titles, String prefix) {
        List<String> res = new ArrayList<>();
        for(var e : titles){
            res.add(e.replace(prefix, ""));
        }
        return res;
    }
    public List<String> addCommonSuffix(List<String> titles){
        List<String> res = new ArrayList<>();
        for(var e : titles){
            res.add(e + " Auto-generated");
        }
        return res;
    }
}
