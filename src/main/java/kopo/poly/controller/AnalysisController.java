package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.ResultDTO;
import kopo.poly.service.IAnalysisService;
import kopo.poly.util.CmmUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@RequestMapping(value = "/analysis")
@RequiredArgsConstructor
@Controller
public class AnalysisController {

    private final IAnalysisService analysisService;

    @GetMapping(value = "/main")
    public String analysisMain(){
        log.info("{}.analysisMain",this.getClass().getName());
        return "analysis/main";
    }

    @GetMapping("/preview")
    public String previewPage() {
        return "/analysis/preview";
    }

    @PostMapping(value = "/preview")
    public String analysisPreview(@RequestParam("image") MultipartFile image, ModelMap model, HttpSession session) throws Exception {

        log.info("{}.analysisPreview",this.getClass().getName());

        if (image.isEmpty()) {
            model.addAttribute("error", "이미지를 선택하세요.");
            return "analysis/main";
        }

        // 이미지 임시 저장 (Base64 인코딩으로 메모리 사용)
        try {
            byte[] bytes = image.getBytes();
            String base64Image = java.util.Base64.getEncoder().encodeToString(bytes);
            session.setAttribute("uploadedImage", image); // 이미지 자체 저장
            model.addAttribute("imagePreview", base64Image);
        } catch (Exception e) {
            model.addAttribute("error", "이미지를 처리하는 중 오류 발생: " + e.getMessage());
            return "analysis/main";
        }
        return "analysis/preview"; // 이미지 미리보기 및 분석 버튼 있는 페이지
    }

    @ResponseBody
    @PostMapping(value = "/analysisProc")
    public String analysisProc(@RequestParam("image") MultipartFile image, HttpSession session) throws Exception {

        log.info("{}.analysisProc Start!", this.getClass().getName());

        if (image == null || image.isEmpty()) {
            return "분석할 이미지가 없습니다.";
        }

        try {
            String result = analysisService.analyzeImage(image);
            session.setAttribute("analysisResult", result);  // 세션에 저장
            return result;
        } catch (Exception e) {
            return "분석 중 오류 발생: " + e.getMessage();
        }
    }

    @GetMapping(value = "/result")
    public String analysisResult(HttpSession session, ModelMap model) {

        log.info("{}.analysisResult Start!",this.getClass().getName());

        String result = (String) session.getAttribute("analysisResult");

        if (result == null) {
            return "redirect:analysis/main";
        }

        session.removeAttribute("uploadedImage");
        model.addAttribute("result", result);

        log.info("{}.analysisResult End!",this.getClass().getName());

        return "analysis/result";
    }

    @ResponseBody
    @PostMapping(value = "/insertResult")
    public MsgDTO insertResult(HttpSession session) throws Exception {

        log.info("{}.insertResult Start!",this.getClass().getName());

        String email= CmmUtil.nvl((String)session.getAttribute("SS_EMAIL"));
        String result = CmmUtil.nvl((String) session.getAttribute("analysisResult"));

        String msg="";
        int res=0;

        if (result == null) {
            msg="error : 저장할 결과가 없습니다.";
        }

        ResultDTO pDTO=ResultDTO.builder()
                .email(email).result(result)
                .build();

        res=analysisService.insertResult(pDTO);

        if(res==1){
            msg="결과가 저장되었습니다.";
        }

        MsgDTO dto=MsgDTO.builder()
                .msg(msg)
                .result(res)
                .build();

        session.removeAttribute("analysisResult");

        log.info("{}.insertResult End!",this.getClass().getName());

        return dto;
    }

    @GetMapping(value = "/resultList")
    public String resultList(HttpSession session, ModelMap model) throws Exception{

        log.info("{}.resultList Start!",this.getClass().getName());

        String email = CmmUtil.nvl((String)session.getAttribute("SS_EMAIL"));

        List<ResultDTO> rList= Optional.ofNullable(analysisService.getResultList(email))
                .orElseGet(ArrayList::new);

        if(rList.isEmpty()){
            return "analysis/resultNone";
        }

        model.addAttribute("rList", rList);

        log.info("{}.resultList End!", this.getClass().getName());

        return "analysis/resultList";
    }

    @GetMapping(value = "/resultInfo")
    public String resultInfo(@RequestParam @NonNull String saveTime, HttpSession session, ModelMap model) throws Exception {

        log.info("{}.resultInfo Start!", this.getClass().getName());

        String email = CmmUtil.nvl((String)session.getAttribute("SS_EMAIL"));

        log.info("saveTime:{}", saveTime);

        ResultDTO pDTO=ResultDTO.builder().email(email).saveTime(saveTime).build();

        ResultDTO mDTO=Optional.ofNullable(analysisService.getResultInfo(pDTO))
                .orElseGet(()->ResultDTO.builder().build());

        String result=mDTO.result();

        ResultDTO rDTO=ResultDTO.builder().email(email).saveTime(saveTime).result(result).build();

        model.addAttribute("rDTO", rDTO);

        log.info("{}.resultInfo End!", this.getClass().getName());

        return "analysis/resultInfo";
    }

}
