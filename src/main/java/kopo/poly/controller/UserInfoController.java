package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.ResultDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IAnalysisService;
import kopo.poly.service.ISelfDiagnosisService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    private final IUserInfoService userInfoService;

    private final ISelfDiagnosisService selfDiagnosisService;
    private final IAnalysisService analysisService;

    @GetMapping(value = "userRegForm")
    public String userRegForm() {
        log.info("{}.userRegForm",this.getClass().getName());
        return "user/userRegForm";
    }

    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public MsgDTO getEmailExists(HttpServletRequest request) throws Exception {

        log.info("{}.getEmailExists Start!",this.getClass().getName());

        String email = request.getParameter("email");

        log.info("email:{}",email);

        UserInfoDTO pDTO=UserInfoDTO.builder().email(EncryptUtil.encAES128CBC(email)).build();

        int res=userInfoService.getEmailExists(pDTO);

        MsgDTO rDTO=MsgDTO.builder().result(res).build();

        log.info("{}.getEmailExists End!",this.getClass().getName());

        return rDTO;
    }

    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {

        log.info("{}.insertUserInfo Start!",this.getClass().getName());

        int res=0;
        String msg="";
        MsgDTO dto;

        UserInfoDTO pDTO;

        try {

            String email = CmmUtil.nvl(request.getParameter("email"));
            String name = CmmUtil.nvl(request.getParameter("name"));
            String password = CmmUtil.nvl(request.getParameter("password"));

            log.info("email:{},name:{},password:{}",email,name,password);

            pDTO=UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128CBC(email))
                    .name(name)
                    .password(EncryptUtil.encHashSHA256(password))
                    .build();

            res=userInfoService.insertUserInfo(pDTO);

            log.info("회원가입 결과(res) : {}",res);

            if(res == 1){
                msg="회원가입이 완료되었습니다.";
            }
            else if(res == 2){
                msg="이미 가입된 이메일입니다.";
            }
            else{
                msg="회원가입이 실패하였습니다.";
            }

        }
        catch(Exception e) {

            msg="오류로 인해 회원가입이 실패하였습니다. "+e;

            log.info(e.toString());

        }
        finally {

            dto=MsgDTO.builder().result(res).msg(msg).build();

            log.info("{}.insertUserInfo End!",this.getClass().getName());

        }

        return dto;
    }

    @GetMapping(value = "login")
    public String login() {
        log.info("{}.login",this.getClass().getName());
        return "user/login";
    }

    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!",this.getClass().getName());

        int res=0;
        String msg="";
        MsgDTO dto;

        UserInfoDTO pDTO;

        try {

            String email = CmmUtil.nvl(request.getParameter("email"));
            String password = CmmUtil.nvl(request.getParameter("password"));

            log.info("email:{},password:{}",email,password);

            pDTO=UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128CBC(email))
                    .password(EncryptUtil.encHashSHA256(password))
                    .build();

            res= userInfoService.getUserLogin(pDTO);

            if(res == 1){
                msg="로그인 되었습니다.";
                session.setAttribute("SS_EMAIL",EncryptUtil.encAES128CBC(email));

            }
            else{
                msg="이메일 또는 비밀번호가 일치하지 않습니다.";
            }

        }
        catch (Exception e) {

            msg="시스템 문제로 로그인이 실패하였습니다.";
            res=2;
            log.info(e.toString());

        }
        finally {

            dto=MsgDTO.builder().result(res).msg(msg).build();

            log.info("{}.loginProc End!",this.getClass().getName());

        }

        return dto;
    }

    @GetMapping(value = "logout")
    public String logout(HttpSession session) {

        log.info("{}.logout Start!",this.getClass().getName());

        session.invalidate();

        return "user/logout";
    }

    @GetMapping(value = "searchPassword")
    public String searchPassword() {
        log.info("{}.searchPassword",this.getClass().getName());
        return "user/searchPassword";
    }

    @ResponseBody
    @PostMapping(value = "searchPasswordProc")
    public MsgDTO searchPasswordProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.searchPasswordProc Start!",this.getClass().getName());

        int res=0,authNumber=0;
        String msg="";
        MsgDTO dto;

        UserInfoDTO pDTO;

        try {

            String email = CmmUtil.nvl(request.getParameter("email"));

            log.info("email:{}",email);

            pDTO=UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128CBC(email))
                    .build();

            res= userInfoService.getEmailExists(pDTO);

            if(res == 1){
                authNumber=userInfoService.sendEmail(pDTO);
                session.setAttribute("SS_EMAIL_AUTH",EncryptUtil.encAES128CBC(email));
                session.setAttribute("authNumber",String.valueOf(authNumber));
                msg="입력하신 이메일로 인증번호를 전송하였습니다.";
            }
            else{
                msg="가입되지 않은 이메일입니다.";
            }

        }
        catch (Exception e) {

            msg="시스템 문제로 이메일 확인이 실패하였습니다.";
            res=2;
            log.info(e.toString());

        }
        finally {

            dto=MsgDTO.builder().result(res).msg(msg).build();

            log.info("{}.searchPasswordProc End!",this.getClass().getName());

        }

        return dto;
    }

    @GetMapping(value = "emailAuth")
    public String emailAuth() {
        log.info("{}.emailAuth",this.getClass().getName());
        return "user/emailAuth";
    }

    @GetMapping(value = "newPassword")
    public String newPassword() {
        log.info("{}.newPassword",this.getClass().getName());
        return "user/newPassword";
    }

    @ResponseBody
    @PostMapping(value = "newPasswordProc")
    public MsgDTO newPasswordProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.newPasswordProc Start!",this.getClass().getName());
        int res=0;
        String msg="";
        MsgDTO dto;

        UserInfoDTO pDTO;

        try {

            String email = CmmUtil.nvl((String) session.getAttribute("SS_EMAIL_AUTH"));
            if(session.getAttribute("SS_EMAIL_AUTH")==null){
                email = CmmUtil.nvl((String) session.getAttribute("SS_EMAIL"));
            }
            String password = CmmUtil.nvl(request.getParameter("password"));

            log.info("email:{},password:{}",email,password);

            pDTO=UserInfoDTO.builder()
                    .email(email)
                    .password(EncryptUtil.encHashSHA256(password))
                    .build();

            res= userInfoService.updatePassword(pDTO);
            if(res == 1){
                msg="비밀번호가 변경되었습니다. 변경된 비밀번호로 로그인해주세요.";
            }
            else{
                msg="비밀번호 변경이 실패하였습니다.";
            }

        }
        catch (Exception e) {
            res=2;
            msg="오류로 인해 비밀번호 변경이 실패하였습니다."+e;
        }
        finally {
            dto= MsgDTO.builder().result(res).msg(msg).build();
            log.info("{}.newPasswordProc End!",this.getClass().getName());
        }

        return dto;
    }

    @GetMapping(value = "myPage")
    public String myPage() {
        log.info("{}.myPage",this.getClass().getName());
        return "user/myPage";
    }

    @GetMapping(value = "delete")
    public String delete() {
        log.info("{}.delete",this.getClass().getName());
        return "user/delete";
    }

    @ResponseBody
    @PostMapping(value = "deleteProc")
    public MsgDTO deleteProc(HttpSession session) throws Exception {

        log.info("{}.deleteProc Start!",this.getClass().getName());

        int res=0;
        String msg="";
        MsgDTO dto;

        UserInfoDTO pDTO;
        ResultDTO qDTO;

        try {
            String email = CmmUtil.nvl((String) session.getAttribute("SS_EMAIL"));

            pDTO=UserInfoDTO.builder().email(email).build();
            userInfoService.deleteUserInfo(pDTO);

            qDTO=ResultDTO.builder().email(email).build();
            analysisService.deleteResult(qDTO);
            selfDiagnosisService.deleteResult(qDTO);

            res=userInfoService.getEmailExists(pDTO);

            if(res == 0){
                res=1;
                msg="회원탈퇴가 완료되었습니다.";
                session.invalidate();
            }
            else{
                res=0;
                msg="회원탈퇴가 실패하였습니다.";
            }
        }
        catch (Exception e) {
            res=2;
            msg="오류로 인해 회원탈퇴가 실패하였습니다. : "+e;
            log.info(e.toString());
        }
        finally {
            dto=MsgDTO.builder().result(res).msg(msg).build();
        }

        log.info("{}.deleteProc End!",this.getClass().getName());

        return dto;
    }

}
