package play.dpl.playlist.Controller;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Service.PlayListService;


@Controller
public class MainController {
    
    @Autowired
    PlayListService playListService;

    @RequestMapping("/")
    public String main() {
        
        return "index";
    }
    @PostMapping("/getPlayList")
    @ResponseBody
    public String getPlayList(@RequestBody String url, HttpServletResponse response){
        System.out.println("받은 URL : "+url);
        System.out.println("getPlayList 실행");
        long s = new Date().getTime();
        
        List<String> titles = playListService.scrapPage(url);
        for(var title : titles){
            System.out.println(title);
        }
        long e = new Date().getTime();
        System.out.println("getPlayList 소요시간 : "+(e-s)/1000f + "초");
        return "1";
    }
}
