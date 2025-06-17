package kopo.poly.controller;

import kopo.poly.dto.CenterDTO;
import kopo.poly.service.IFindNearService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/findnear")
@Controller
public class FindNearController {

    private final IFindNearService findNearService;

    @GetMapping(value = "/main")
    public String findNearMain(){
        return "findnear/main";
    }

    @GetMapping(value = "/find")
    @ResponseBody
    public List<CenterDTO> getNearCenter(@RequestParam double lat, @RequestParam double lng) {

        log.info("{}.getNearCenter Start!", this.getClass().getName());

        CenterDTO pDTO;
        List<CenterDTO> rList=null;

        try {
            pDTO=CenterDTO.builder().lat(lat).lng(lng).build();
            rList=findNearService.getNearCenters(pDTO);
        }
        catch (Exception e) {
            log.info("error : {}",e.toString());
        }
        finally {
            log.info("{}.getNearCenter End!", this.getClass().getName());
        }
        return rList;
    }

}
