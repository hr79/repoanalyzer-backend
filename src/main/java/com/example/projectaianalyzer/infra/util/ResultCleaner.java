package com.example.projectaianalyzer.infra.util;

public class ResultCleaner {
    public static String getCleanResult(String result) {
        return result.replaceAll("(?s)<think>.*?</think>", "")
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }
}
