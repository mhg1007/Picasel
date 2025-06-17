package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.ResultDTO;
import kopo.poly.service.ISelfDiagnosisService;
import kopo.poly.util.CmmUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping(value = "/selfdiagnosis")
@RequiredArgsConstructor
@Controller
public class SelfDiagnosisController {

    private final ISelfDiagnosisService selfDiagnosisService;

    @GetMapping(value = "/main")
    public String SelfDiagnosisMain() throws Exception{
        log.info("{}.SelfDiagnosisMain",this.getClass().getName());
        return "selfdiagnosis/main";
    }

    @ResponseBody
    @PostMapping(value = "/result")
    public MsgDTO result(HttpServletRequest request) throws Exception {

        log.info("{}.result Start!", this.getClass().getName());

        String msg = "";
        int res=1;
        MsgDTO dto;

        try {
            int score = Integer.parseInt(CmmUtil.nvl(request.getParameter("totalnum")));
            log.info("result score: {}", score);

            if(score<21){
                msg="정상적인 상태입니다. ";
            }
            else if (score<28) {
                msg="우울 경향이 어느 정도 있는 상태의 위험군입니다. ";
            }
            else{
                msg="매우 우울한 상태의 고위험군입니다. ";
            }
        }
        catch (Exception e) {
            res=0;
            msg=e.toString();
            log.info("error: {}",msg);
        }
        finally {
            if (res!=0) {
                msg += "이 검사는 선별검사로 활용되는 자기보고형 척도검사로, 결과 자체가 정확한 진단은 아닙니다. 정확한 판단을 위해서는 전문기관의 추가검사 및 전문가 상담이 필요합니다.";
            }
            dto=MsgDTO.builder().msg(msg).result(res).build();
            log.info("{}.result End!", this.getClass().getName());
        }

        return dto;
    }

    @ResponseBody
    @PostMapping(value = "/saveResult")
    public MsgDTO saveResult(HttpServletRequest request, HttpSession session) throws Exception {
        log.info("{}.saveResult Start!", this.getClass().getName());

        String msg = "";
        int res=0;
        MsgDTO dto;

        try {
            String email = CmmUtil.nvl((String) session.getAttribute("SS_EMAIL"));
            String result= CmmUtil.nvl(request.getParameter("msg"));

            ResultDTO pDTO=ResultDTO.builder()
                    .email(email).result(result)
                    .build();

            res=selfDiagnosisService.insertResult(pDTO);

            if(res==1){
                msg="결과가 저장되었습니다.";
            }
            else{
                msg="결과 저장에 실패하였습니다.";
            }

        }
        catch (Exception e) {
            msg="오류로 인해 저장에 실패하였습니다. : "+e;
            log.info("error: {}",msg);
        }
        finally {
            dto=MsgDTO.builder().msg(msg).result(res).build();
            log.info("{}.saveResult End!", this.getClass().getName());
        }

        return dto;
    }

    @GetMapping(value = "/resultList")
    public String resultList(HttpSession session, ModelMap model) throws Exception{

        log.info("{}.resultList Start!",this.getClass().getName());

        String email = CmmUtil.nvl((String)session.getAttribute("SS_EMAIL"));

        List<ResultDTO> rList= Optional.ofNullable(selfDiagnosisService.getResultList(email))
                .orElseGet(ArrayList::new);

        if(rList.isEmpty()){
            return "selfdiagnosis/resultNone";
        }

        model.addAttribute("rList", rList);

        log.info("{}.resultList End!", this.getClass().getName());

        return "selfdiagnosis/resultList";
    }

    @GetMapping(value = "/resultInfo")
    public String resultInfo(@RequestParam @NonNull String saveTime, HttpSession session, ModelMap model) throws Exception {

        log.info("{}.resultInfo Start!", this.getClass().getName());

        String email = CmmUtil.nvl((String)session.getAttribute("SS_EMAIL"));

        log.info("saveTime:{}", saveTime);

        ResultDTO pDTO=ResultDTO.builder().email(email).saveTime(saveTime).build();

        ResultDTO mDTO=Optional.ofNullable(selfDiagnosisService.getResultInfo(pDTO))
                .orElseGet(()->ResultDTO.builder().build());

        String res=mDTO.result();
        String result="";
        if(res.equals("정상")){
            result="정상적인 상태입니다. ";
        }
        else if(res.equals("우울")){
            result="우울 경향이 어느 정도 있는 상태의 위험군입니다. ";
        }
        else if(res.equals("매우 우울")){
            result="매우 우울한 상태의 고위험군입니다. ";
        }
        result+="이 검사는 선별검사로 활용되는 자기보고형 척도검사로, 결과 자체가 정확한 진단은 아닙니다. 정확한 판단을 위해서는 전문기관의 추가검사 및 전문가 상담이 필요합니다.";

        ResultDTO rDTO=ResultDTO.builder().email(email).saveTime(saveTime).result(result).build();

        model.addAttribute("rDTO", rDTO);

        log.info("{}.resultInfo End!", this.getClass().getName());

        return "selfdiagnosis/resultInfo";
    }

}
