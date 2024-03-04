package play.dpl.playlist.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import play.dpl.playlist.Service.MemberService;

@Controller
public class MemberController {

    private final MemberService memberService;
    
    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }
}