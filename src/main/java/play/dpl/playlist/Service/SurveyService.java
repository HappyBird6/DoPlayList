package play.dpl.playlist.Service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Entity.Survey;
import play.dpl.playlist.Repository.SurveyRepository;

@Service
public class SurveyService {
    @Autowired
    private SurveyRepository surveyRepository;

    public long vote(int type){
        Survey survey = new Survey();
        try{
            if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
                Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getMember();
                
                Optional<Long> maxId = surveyRepository.findMaxId();
                if(maxId.isPresent()){
                    survey.setId(maxId.get()+1);
                }else{
                    survey.setId(1l);
                }
                String id = member.getEmail();
                survey.setMemberId(id);
                survey.setSurveyAt(new Date());
                survey.setType(type);
                survey.setDetail("");  
                surveyRepository.save(survey);       
                return maxId.get()+1;   
            }else{
                return 2;
            }
        }catch(Exception e){
            // System.out.println(e.getMessage());
            return 2;
        }

    }
    public int vote(String detail){
        try{
            if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
                Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getMember();
                
                String[] datas = detail.split("#");
                Long id = Long.parseLong(datas[0]);
                int inaccurate = Integer.parseInt(datas[1]);
                int inconvenience = Integer.parseInt(datas[2]);
                int etc = Integer.parseInt(datas[3]);
                String opinion = "";
                if( datas.length==5 &&(datas[4]!=null || !datas[4].equals(""))){
                    opinion = datas[4];
                }
                String data = inaccurate +"#"+ inconvenience + "#"+etc+"#"+opinion;
                Optional<Survey> survey = surveyRepository.findById(id);
                if(survey.isPresent()){
                    Survey s= survey.get();
                    s.setDetail(data);
                    surveyRepository.save(s);
                    return 1;
                }else{
                    return 2;
                }
            }
        }catch(Exception e){
            // System.out.println(e.getMessage());
            return 2;
        }
        return 2;
    }
}
