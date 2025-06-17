package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.ResultDTO;
import kopo.poly.repository.AnalysisRepository;
import kopo.poly.repository.impl.AnalysisEntity;
import kopo.poly.service.IAnalysisService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalysisService implements IAnalysisService {

    private final AnalysisRepository analysisRepository;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final ObjectMapper objectMapper;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String analyzeImage(MultipartFile imageFile) throws Exception {

        log.info("{}.analyzeImage Start!",this.getClass().getName());

        List<String> detections = runObjectDetection(imageFile);

        // ChatGPT API 호출
        String prompt = buildPrompt(detections);
        String gptResult = sendToGPT(prompt);

        log.info("gptResult: {}", gptResult);

        log.info("{}.analyzeImage End!",this.getClass().getName());

        return gptResult;
    }

    public String putS3(String keyName, Path filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        // 파일 업로드
        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(filePath));

        // 업로드된 파일의 URL 생성
        return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
    }

    @Override
    public List<String> runObjectDetection(MultipartFile imageFile) throws Exception {

        log.info("{}.runObjectDetection Start!", this.getClass().getName());

        String uuid = UUID.randomUUID().toString();
        List<String> results = new ArrayList<>();

        // 1. MultipartFile → 임시 파일로 저장
        Path tempFile = Files.createTempFile(uuid, ".jpg");
        imageFile.transferTo(tempFile.toFile());

        // 2. S3에 업로드
        String keyName = "uploads/" + tempFile.getFileName();
        log.info("keyName: {}", keyName);

        String url = putS3(keyName, tempFile);
        log.info("url: {}", url);

        // 3. 임시 파일 삭제
        Files.deleteIfExists(tempFile);

        // 4. Python Flask 서버로 JSON POST 요청
//        String pythonServerUrl = "http://127.0.0.1:5000/object_detection";
        String pythonServerUrl = "http://kopo-traine-flask-servic-6b412-106184133-101595ffcec6.kr.lb.naverncp.com:5000/object_detection";

        // JSON 요청 객체 생성
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("url", url);

        String jsonRequest = objectMapper.writeValueAsString(requestMap);

        RequestBody requestBody = RequestBody.create(
                jsonRequest,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(pythonServerUrl)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // OkHttp3로부터 받은 JSON 응답 문자열
            String responseBody = response.body().string();

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> objectList = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>(){});

            for (Map<String, Object> obj : objectList) {
                String className = obj.get("class").toString();
                String confidence = obj.get("confidence").toString();
                String bboxRatio = obj.get("bbox_ratio").toString();

                // 원하는 형식으로 포맷
                String formatted = String.format("클래스: %s, 정확도: %s, 비율: %s", className, confidence, bboxRatio);
                log.info("formatted: {}", formatted);
                results.add(formatted);
            }

        } catch (IOException e) {
            log.error("Flask 서버 요청 실패: {}", e.getMessage());
        }

        log.info("{}.runObjectDetection End!", this.getClass().getName());
        return results;
    }

    @Override
    public String buildPrompt(List<String> detections) throws Exception {
        if (detections.isEmpty()) {
            return "그림에서 특별한 객체가 감지되지 않았습니다.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 아동 그림에서 감지된 시각 요소들입니다:\n");
        for (String obj : detections) {
            sb.append("- ").append(obj).append("\n");
        }

        sb.append("\n이 그림은 아동의 정서와 내면을 반영하는 표현입니다. 다음 조건을 지켜서 해석해 주세요:\n");
        sb.append("• 각 요소가 상징하는 심리적 의미를 반영해 주세요.\n");
        sb.append("• 번호나 항목 없이 자연스러운 문장으로 해석해 주세요.\n");
        sb.append("• 전체 분량은 300자 이내로 간결하게 요약해 주세요.\n");
        sb.append("• 실제 아동 심리상담사가 작성한 리포트처럼 진단적 문체로 작성해 주세요.\n");

        return sb.toString();
    }

    @Override
    public String sendToGPT(String prompt) throws Exception {

        log.info("{}.sendToGPT Start!",this.getClass().getName());

        // chatGPT API 호출 (system+user 구성도 가능)
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 아동 그림을 기반으로 심리 상태를 분석하는 전문 아동심리상담사입니다."),
                        Map.of("role", "user", "content", prompt)
                )
        ));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + openaiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("OpenAI API error: " + response);

            String responseBody = response.body().string();
            JsonNode json = objectMapper.readTree(responseBody);

            log.info("{}.sendToGPT End!",this.getClass().getName());

            return json.get("choices").get(0).get("message").get("content").asText();
        }
    }

    @Override
    public int insertResult(ResultDTO pDTO) throws Exception {

        log.info("{}.insertResult Start!",this.getClass().getName());

        int res=0;

        String email= CmmUtil.nvl(pDTO.email());
        String result= CmmUtil.nvl(pDTO.result());
        String saveTime= CmmUtil.nvl(DateUtil.getDateTime("yyyy-MM-dd HH:mm"));

        AnalysisEntity pEntity=AnalysisEntity.builder()
                .email(email).result(result).saveTime(saveTime)
                .build();

        analysisRepository.insert(pEntity);

        Optional<AnalysisEntity> rEntity=analysisRepository.findByEmailAndSaveTime(email,saveTime);

        if(rEntity.isPresent()) {
            res=1;
        }

        log.info("{}.insertResult End!",this.getClass().getName());

        return res;
    }

    @Override
    public List<ResultDTO> getResultList(String email) throws Exception {

        log.info("{}.getResultList Start!",this.getClass().getName());

        List<AnalysisEntity> rList = analysisRepository
                .findAllByEmailOrderBySaveTimeDesc(email)
                .orElseGet(ArrayList::new);

        List<ResultDTO> nList = rList.stream().map(entity ->
                ResultDTO.builder()
                        .email(entity.getEmail())
                        .saveTime(entity.getSaveTime())
                        .result(entity.getResult())
                        .build()
        ).toList();

        log.info("{}.getResultList End!", this.getClass().getName());

        return nList;
    }

    @Override
    public ResultDTO getResultInfo(ResultDTO pDTO) throws Exception {
        log.info("{}.getNoticeInfo Start!", this.getClass().getName());

        Optional<AnalysisEntity> rEntity=analysisRepository.findByEmailAndSaveTime(pDTO.email(), pDTO.saveTime());

        ResultDTO rDTO = rEntity.map(entity -> ResultDTO.builder()
                .email(entity.getEmail())
                .saveTime(entity.getSaveTime())
                .result(entity.getResult())
                .build()
        ).orElseGet(() -> ResultDTO.builder().build());

        log.info("{}.getNoticeInfo End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public void deleteResult(ResultDTO pDTO) throws Exception {

        log.info("{}.deleteResult Start!", this.getClass().getName());

        String email= CmmUtil.nvl(pDTO.email());

        analysisRepository.deleteByEmail(email);

        log.info("{}.deleteResult End!", this.getClass().getName());

    }
}
