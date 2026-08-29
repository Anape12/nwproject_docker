package jp.nw.model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AiServiceClient {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String baseUrl = env("AI_SERVICE_URL", "http://localhost:8000");
    private final String token = env("AI_SERVICE_TOKEN", "");

    public String respond(long characterId, String name, String prompt, String personality, String interests,
            String model, String type, String conversationId, String context, String message) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("character_id", String.valueOf(characterId));
        body.put("character_name", safe(name));
        body.put("system_prompt", safe(prompt));
        body.put("personality", safe(personality));
        body.put("interests", safe(interests));
        if (model != null && !model.isBlank()) body.put("model_name", model.trim());
        body.put("conversation_type", safe(type));
        body.put("conversation_id", safe(conversationId));
        body.put("context", safe(context));
        body.put("message", safe(message));
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + "/internal/respond"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(90)).header("Content-Type", "application/json; charset=UTF-8");
        if (!token.isBlank())
            b.header("X-Internal-Token", token);
        HttpResponse<String> res = client.send(
                b.POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body))).build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new IllegalStateException("AI service returned " + res.statusCode() + ": " + res.body());
        String answer = JSON.readTree(res.body()).path("answer").asText().trim();
        if (answer.isBlank())
            throw new IllegalStateException("AI service returned an empty answer");
        return answer.length() > 500 ? answer.substring(0, 497) + "…" : answer;
    }

    private static String env(String k, String d) {
        String v = System.getenv(k);
        return v == null || v.isBlank() ? d : v;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
