package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping(value = "/")
@Controller
public class MainController {

    @GetMapping(value = "index")
    public String index(HttpSession session) {
        return session.getAttribute("SS_EMAIL")==null? "/index" : "/indexLogin";
    }

}
