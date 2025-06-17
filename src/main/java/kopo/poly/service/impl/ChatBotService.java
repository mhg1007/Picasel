package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.service.IChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatBotService implements IChatBotService {

    private final LambdaClient lambdaClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${lambda.retrieve.functionName}")
    private String lambdaFunctionName;

    private final ObjectMapper objectMapper;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String chat(String userInput) throws Exception {
        List<String> docs=retrieve(userInput);
        String prompt = buildPrompt(userInput, docs);
        return sendToGPT(prompt);
    }

    @Override
    public List<String> retrieve(String userInput) throws Exception {

        String payload = objectMapper.writeValueAsString(Map.of("query", userInput));

        InvokeResponse response = lambdaClient.invoke(
                InvokeRequest.builder()
                        .functionName(lambdaFunctionName)
                        .payload(SdkBytes.fromUtf8String(payload))
                        .build()
        );

        String json = response.payload().asUtf8String();
        JsonNode root = objectMapper.readTree(json);
        if (!root.has("body")) {
            throw new IllegalStateException("Lambda 응답에 'body' 필드가 없습니다. 실제 응답: " + json);
        }

        String body = root.get("body").asText();
        return objectMapper.readValue(body, new TypeReference<List<String>>() {});

    }

    @Override
    public String buildPrompt(String userInput, List<String> retrievedDocs) throws Exception {
        String context = String.join("\n---\n", retrievedDocs);

        return """
            아래는 다른 아동·청소년의 상담 기록에서 검색된 참고용 문서입니다:
        
            %s
        
            사용자의 질문: %s
        
            참고용 문서는 질문을 한 사용자가 겪은 일은 아닙니다.
            참고용 문서의 내용은 사용자의 질문 내용에 반응하기 어려운 경우에만 질문 형식으로 사용해 주세요.
            아동·청소년 상담 전문가로서,
            사용자의 질문에 대해 공감하며 친절하게 반응하는 답변을 두 문장 이내로 해주세요.
            """.formatted(context, userInput);
    }

    @Override
    public String sendToGPT(String prompt) throws Exception {
        log.info("{}.sendToGPT Start!",this.getClass().getName());

        // chatGPT API 호출 (system+user 구성도 가능)
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 아동·청소년 상담 전문가입니다."),
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
}
