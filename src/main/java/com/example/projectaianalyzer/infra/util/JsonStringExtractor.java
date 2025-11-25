package com.example.projectaianalyzer.infra.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonStringExtractor {
    public static String extractJsonFromString(String input) {
        log.info(":::: 문자열에서 json 형식의 부분 추출 시작 ::::");
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string is empty");
        }

        String content = input.replaceAll("(?s)<think>.*?</think>", "")
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // json이 배열이 아닌 하나일 경우
        if (content.startsWith("{") && content.endsWith("}")) {
            return content;
        }

        int start = content.indexOf("[");
        int end = content.lastIndexOf("]");

        // content가 온전한 json 구조만 존재한다면 그대로 return
        if (start == 0 && end == content.length() - 1) {
            return content;
        }

        if(start == -1 || end == -1||start > end){
            throw new IllegalArgumentException("JSON 블록을 찾을 수 없습니다.");
        }
        return content.substring(start, end+1);
    }
}
