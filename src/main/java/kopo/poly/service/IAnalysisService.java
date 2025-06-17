package kopo.poly.service;

import kopo.poly.dto.ResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAnalysisService {

    String analyzeImage(MultipartFile imageFile) throws Exception;

    List<String> runObjectDetection(MultipartFile imageFile) throws Exception;

    String buildPrompt(List<String> detections) throws Exception;

    String sendToGPT(String prompt) throws Exception;

    int insertResult(ResultDTO pDTO) throws Exception;

    List<ResultDTO> getResultList(String email) throws Exception;

    ResultDTO getResultInfo(ResultDTO pDTO) throws Exception;

    void deleteResult(ResultDTO pDTO) throws Exception;
}
