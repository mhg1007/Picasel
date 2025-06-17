package kopo.poly.service;

import java.util.List;

public interface IChatBotService {
    String chat(String userInput) throws Exception;
    List<String> retrieve(String userInput) throws Exception;
    String buildPrompt(String userInput, List<String> retrievedDocs) throws Exception;
    String sendToGPT(String prompt) throws Exception;
}
