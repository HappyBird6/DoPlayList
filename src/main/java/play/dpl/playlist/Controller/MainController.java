package play.dpl.playlist.Controller;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Entity.Music;
import play.dpl.playlist.Entity.Playlist;
import play.dpl.playlist.Service.MemberInfoDetails;
import play.dpl.playlist.Service.MemberService;
import play.dpl.playlist.Service.PlaylistService;
import play.dpl.playlist.Service.SurveyService;
import play.dpl.playlist.Service.YoutubeService;


@Controller
public class MainController {
    
    @Autowired
    PlaylistService playlistService;
    @Autowired
    YoutubeService youtubeService;
    @Autowired
    SurveyService surveyService;
    @Autowired
    MemberService memberService;

    @RequestMapping("/")
    public ModelAndView main(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        System.out.println("접속 IP : "+clientIp+" / "+new Date());
        ModelAndView mav = new ModelAndView("index");
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();

            String playlistList = member.getPlaylistList();
            Map<String,String> list = new HashMap<>();
            String[] playlist = playlistList.split(",");
            for(int i = 0;i<playlist.length;i++){
                list.put(playlist[i].split("#")[0],playlist[i].split("#")[1]);
            }
            List<Object[]> history = memberService.getHistory(member.getEmail());

            mav.addObject("playlistList",list);
            mav.addObject("history",history);
            mav.addObject("email",member.getEmail());
        }else{
            // System.out.println("로그인X");
        }
        return mav;
    }
    @PostMapping("/getPlayList")
    @ResponseBody
    public String[] getPlayList(@RequestBody String url, HttpServletResponse response){

        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();
        }else{
            // return new String[]{"-1"};
        }   
        Playlist playlist = playlistService.getPlaylist(url);       
        
        return new String[]{"1",playlist.getYtChannel(),playlist.getYtTitle(),playlist.getYtMusicTitlesJson()};
    }

    
    @PostMapping("/getMusicData")
    @ResponseBody
    public String getMusic(@RequestBody String data, HttpServletResponse response){
        
        Music music = playlistService.getMusicData(data.split("#")[0],data.split("#")[1]);
        // musicData : {music 유튜브 id, thumbnail 주소,link, 해당 유튜브 제목}
        if(music==null){
            return "-1";
        }
        return new Gson().toJson(music);
    }

    @PostMapping("/submitSelection")
    @ResponseBody
    public String submitSelection(@RequestBody String data, HttpServletResponse response) throws JsonMappingException, JsonProcessingException{
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(data);
        String accessCode = "";
        List<Integer> flagList = new ArrayList<>();
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();
            accessCode = member.getAccessCode();
        }

        for (JsonNode node : jsonNode) {
            String videoId = node.get("videoId").asText();
            String ytChannel = node.get("ytChannel").asText();
            String ytTitle = node.get("ytTitle").asText();
            Music music = Music.builder().id(videoId).ytChannel(ytChannel).ytTitle(ytTitle).build();
            String playlistId = node.get("playlistId").asText();

            try{
                youtubeService.addMusicToPlayList(accessCode, playlistId, music);
            }catch(Exception e){
                flagList.add(2);
                continue;
            }
            flagList.add(1);
        }
        return new Gson().toJson(flagList);
        // flag  0:변화x, 2:에러, 1:정상
    }

    @PostMapping("/vote")
    @ResponseBody
    public long vote(@RequestBody String data,HttpServletResponse response) throws JsonMappingException, JsonProcessingException{
        
        return surveyService.vote(Integer.parseInt(data));
    }

    @PostMapping("/voteDetail")
    @ResponseBody
    public int voteDetail(@RequestBody String data,HttpServletResponse response) throws JsonMappingException, JsonProcessingException{

        return surveyService.vote(data);
    }

    @PostMapping("/refreshPlaylist")
    @ResponseBody
    public List<String[]> refreshPlaylist() throws GoogleJsonResponseException, GeneralSecurityException, IOException {
        List<String[]> entity = new ArrayList<>();
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();
            List<String[]> list = youtubeService.getPlayList(member.getAccessCode(),null);
            String playlistList =""; 
            for(var e : list){
                String temp = e[1]+"#"+e[0]+",";
                playlistList += temp;        
                entity.add(new String[]{e[1],e[0]});       
            }
            if(!playlistList.equals("")) playlistList.substring(0,playlistList.length()-1);
            member.setPlaylistList(playlistList);
            memberService.saveMember(member);
            return entity;
        }
        return null;
    }
    

    @GetMapping("/PrivacyAgreement")
    public String displayPrivacyAgreement() {
        return "PrivacyAgreement";
    }
    @GetMapping("/PrivacyPolicy")
    public String displayPrivacyPolicy() {
        return "PrivacyPolicy";
    }
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
