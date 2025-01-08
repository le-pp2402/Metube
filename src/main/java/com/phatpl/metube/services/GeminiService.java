package com.phatpl.metube.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatpl.metube.utils.Constant;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class GeminiService {
    @Value("${GOOGLE_KEY}")
    private String apiKey;

    @Value("${GOOGLE_API}")
    private String link;

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private final String sysInstruction = "Summarize the following passage less than 100 words: ";

    private String createJSONPromt(String query) {
        var text = new HashMap<String, Object>();
        text.put("text", sysInstruction + query);

        var parts = new HashMap<String, ArrayList<Object>>();
        var part = new ArrayList<>();
        part.add(text);
        parts.put("parts", part);

        var contents = new HashMap<String, ArrayList<Object>>();
        var content = new ArrayList<>();
        content.add(parts);
        contents.put("contents", content);

        JSONObject json = new JSONObject(contents);
        return json.toString();
    }

    public String generator(String query) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(createJSONPromt(query)))
                    .uri(URI.create(link + apiKey))
                    .header("Content-type", Constant.CT_APPLICATION_JSON)
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return extractInformation(res);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractInformation(HttpResponse<String> response) throws JsonProcessingException {
        var result = new ObjectMapper().readValue(response.body(), HashMap.class);
        var result1 = (ArrayList<HashMap<String, Object>>) result.get("candidates");
        var result2 = (HashMap<String, Object>) result1.get(0).get("content");
        var result3 = (ArrayList<HashMap<String, Object>>) result2.get("parts");
        return (String) result3.get(0).get("text");
    }
}
