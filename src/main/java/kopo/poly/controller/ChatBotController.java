package kopo.poly.controller;

import kopo.poly.service.IChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/chatbot")
@Controller
public class ChatBotController {

    private final IChatBotService chatBotService;

    @GetMapping(value = "/main")
    public String chat() throws Exception {
        return "chatbot/main";
    }

    @PostMapping(value = "/chat")
    @ResponseBody
    public String chat(@RequestBody String userInput) throws Exception{
        return chatBotService.chat(userInput);
    }

}
