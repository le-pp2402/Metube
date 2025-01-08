package com.phatpl.metube.utils;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class TextToJsonArray {
    public static String ReadFile(InputStream file) throws IOException {
        byte[] line = file.readAllBytes();
        StringBuilder content = new StringBuilder();
        for (byte b : line) {
            if (Character.isLetter(b) || Character.isDigit(b)) {
                content.append(Character.toString(b));
            } else if (Character.isSpaceChar(b)) {
                content.append(" ");
            }
        }
        return content.toString();
    }

    public static JSONArray toJsonArray(String str) {
        String[] words = str.split(" ");
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (var word : words) map.put(word, true);
        JSONArray jsonArray = new JSONArray();
        map.forEach((key, value) -> jsonArray.put(key));
        return jsonArray;
    }
}
